package org.mbari.mondrian.services.vars;

import okhttp3.HttpUrl;
import okhttp3.Request;
import org.mbari.mondrian.domain.Counter;
import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.etc.gson.Json;
import org.mbari.mondrian.etc.jdk.Logging;
import org.mbari.mondrian.etc.okhttp3.ClientSupport;
import org.mbari.mondrian.services.ImageService;
import org.mbari.mondrian.util.FetchRange;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.EndpointConfig;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.services.model.Media;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class AnnosaurusImageService implements ImageService {

    private final org.mbari.vars.services.AnnotationService annotationService;
    private final MediaService mediaService;
    private final ClientSupport clientSupport;
    private final EndpointConfig annosaurusEndpointConfig;
    public record MediaCount(Media media, Long imageCount) {}
    public record MediaRequest(Media media, int limit, int offset) {}
    private static final Logging log = new Logging(AnnosaurusImageService.class);

    public AnnosaurusImageService(AnnotationService annotationService,
                                  MediaService mediaService,
                                  ClientSupport clientSupport,
                                  EndpointConfig annosaurusEndpointConfig) {
        this.annotationService = annotationService;
        this.mediaService = mediaService;
        this.clientSupport = clientSupport;
        this.annosaurusEndpointConfig = annosaurusEndpointConfig;
    }

    @Override
    public CompletableFuture<Optional<Image>> findByUuid(UUID uuid) {
        return annotationService.findImageByUuid(uuid)
                .thenApply(Optional::ofNullable);
    }

    @Override
    public CompletableFuture<Page<Image>> findByMediaUuid(UUID mediaUuid, int size, int page) {
        return countImagesByMediaUuid(mediaUuid)
                .thenCompose(counter ->
                    fetchImagesByMediaUuid(mediaUuid, size, page)
                            .thenApply(images -> new Page<>(images, size, page, counter.count().longValue()))
                );
    }

    private CompletableFuture<List<Image>> fetchImagesByMediaUuid(UUID videoReferenceUuid, int size, int page) {
        var limit = (long) size;
        var offset = (long) page * size;
        return fetchImagesByMediaUuid(videoReferenceUuid, limit, offset);
    }

    private CompletableFuture<List<Image>> fetchImagesByMediaUuid(UUID videoReferenceUuid, long limit, long offset) {
        var url = annosaurusEndpointConfig.getUrl() + "/fast/images/videoreference/" + videoReferenceUuid;
        var httpUrl = HttpUrl.parse(url).newBuilder()
                .addQueryParameter("limit", String.valueOf(limit))
                .addQueryParameter("offset", String.valueOf(offset))
                .build();
        var request = new Request.Builder()
                .url(httpUrl)
                .header("Accept", "application/json")
                .build();
        return clientSupport.execRequest(request, s -> {
            log.atDebug().log(s);
            return Json.decodeArray(s, Image.class);
        });
    }

    @Override
    public CompletableFuture<Counter> countImagesByMediaUuid(UUID mediaUuid) {
        var request = new Request.Builder()
                .url(annosaurusEndpointConfig.getUrl() + "/fast/images/count/videoreference/" + mediaUuid)
                .header("Accept", "application/json")
                .build();
        return clientSupport.execRequest(request, s -> Json.decode(s, Counter.class));
    }

    private CompletableFuture<Page<Image>> findImages(List<Media> media, int size, int page) {
        var totalCount = new AtomicLong(0);
        return AsyncUtils.collectAll(media, m -> countImagesByMediaUuid(m.getVideoReferenceUuid())
                        .thenApply(c -> new MediaCount(m, c.count().longValue())))
                .thenApply(xs -> {
                    var total = xs.stream().mapToLong(MediaCount::imageCount).sum();
                    totalCount.set(total);
                    return xs;
                })
                .thenApply(xs -> xs.stream().sorted(Comparator.comparing(a -> a.media().getStartTimestamp())).toList())
                .thenApply(xs -> FetchRange.fromPage(xs, size, page, MediaCount::imageCount))
                .thenCompose(xs -> AsyncUtils.collectAll(xs, mc -> fetchImagesByMediaUuid(mc.data().media().getVideoReferenceUuid(), mc.limit(), mc.offset())))
                .thenApply(xs -> {
                    var image = xs.stream().flatMap(List::stream).toList();
                    return new Page<>(image, size, page, totalCount.get());
                });
    }

    @Override
    public CompletableFuture<Page<Image>> findByVideoSequenceName(String videoSequenceName, int size, int page) {
        return  mediaService.findByVideoSequenceName(videoSequenceName)
                .thenCompose(media -> findImages(media, size, page));
    }

    @Override
    public CompletableFuture<Page<Image>> findByVideoName(String videoName, int size, int page) {
        return mediaService.findByVideoName(videoName)
                .thenCompose(media -> findImages(media, size, page));
    }

    @Override
    public CompletableFuture<Page<Image>> findByConceptName(String conceptName, int size, int page, boolean includeDescendants) {
        var limit = (long) size;
        var offset = (long) page * size;
        // Need to use httpclient to call a /anno/v1/fast/
        annotationService.countObservationsByConcept(conceptName);
        return annotationService.findByConcept(conceptName, limit, offset, includeDescendants)
                .thenApply(annotations -> annotations.stream()
                        .flatMap(a -> toImages(a).stream())
                        .toList())
                .thenApply(images -> new Page<>(images, size, page, -1L));
    }

    public static List<Image> toImages(Annotation a) {
        return a.getImages()
                .stream()
                .map(ir -> {
                    var i = new Image();
                    i.setUrl(ir.getUrl());
                    i.setDescription(ir.getDescription());
                    i.setImagedMomentUuid(a.getImagedMomentUuid());
                    i.setImageReferenceUuid(ir.getUuid());
                    i.setElapsedTime(a.getElapsedTime());
                    i.setTimecode(a.getTimecode());
                    i.setRecordedTimestamp(a.getRecordedTimestamp());
                    i.setFormat(ir.getFormat());
                    i.setVideoReferenceUuid(a.getVideoReferenceUuid());
                    return i;
                })
                .toList();

    }
}
