package org.mbari.mondrian.services.vars;

import okhttp3.HttpUrl;
import okhttp3.Request;
import org.mbari.jcommons.math.Matlib;
import org.mbari.mondrian.domain.Counter;
import org.mbari.mondrian.domain.Page;
import org.mbari.mondrian.etc.gson.Json;
import org.mbari.mondrian.etc.okhttp3.ClientSupport;
import org.mbari.mondrian.services.ImageService;
import org.mbari.mondrian.util.FetchRange;
import org.mbari.vars.core.util.AsyncUtils;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.MediaService;
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
        return clientSupport.execRequest(request, s -> Json.decodeArray(s, Image.class));
    }

    @Override
    public CompletableFuture<Counter> countImagesByMediaUuid(UUID mediaUuid) {
        var request = new Request.Builder()
                .url(annosaurusEndpointConfig.getUrl() + "/fast/images/count/videoreference/" + mediaUuid)
                .header("Accept", "application/json")
                .build();
        return clientSupport.execRequest(request, s -> Json.decode(s, Counter.class));
    }

    @Override
    public CompletableFuture<Page<Image>> findByVideoSequenceName(String videoSequenceName, int size, int page) {
        // get videoReferenceUuids for videoSequenceName order by start date of video sequence
        var totalCount = new AtomicLong(0);
        return  mediaService.findByVideoSequenceName(videoSequenceName)
                .thenCompose(media ->
                    AsyncUtils.collectAll(media, m -> countImagesByMediaUuid(m.getVideoReferenceUuid())
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
                            })
                );
        // get count for each videoRef
        // Calculate fetch

    }


    @Override
    public CompletableFuture<Page<Image>> findByVideoName(String videoName, int size, int page) {
        return null;
    }

    @Override
    public CompletableFuture<Page<Image>> findByConceptName(String conceptName, int size, int page, boolean includeDescendants) {
        return null;
    }
}
