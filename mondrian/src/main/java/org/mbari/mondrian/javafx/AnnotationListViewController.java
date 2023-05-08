package org.mbari.mondrian.javafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import org.mbari.mondrian.domain.Selection;
import org.mbari.vars.services.model.Annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Displays a selecteable lsit of annotations
 * Usage:
 * <pre>
 *     <code>
 *         var controller = new AnnotationListViewController();
 *         Consumer<Selection<List<Annotation>>> fn = selection -> {
 *             if (selection.source() != controller) {
 *                 System.out.println(selection.selected());
 *             }
 *         }
 *         controller.setOnAnnotationSelection(fn):
 *         var listView = controller.getListView();
 *         // Add listview to scene
 *         controller.setAnnotations(myAnnotations);
 *     </code>
 * </pre>
 */
public class AnnotationListViewController {

    private ListView<Annotation> listView;
    private Consumer<Selection<Collection<Annotation>>> onAnnotationSelection = selection -> {};

    public AnnotationListViewController() {
        init();
    }

    public void setOnAnnotationSelection(Consumer<Selection<Collection<Annotation>>> onAnnotationSelection) {
        if (onAnnotationSelection == null) {
            onAnnotationSelection = selection -> {};
        }
        this.onAnnotationSelection = onAnnotationSelection;
    }

    public void setAnnotations(Collection<Annotation> annotations) {
        Platform.runLater(() -> listView.getItems().setAll(annotations));
    }

    public void setSelectedAnnotations(Collection<Annotation> annotations) {
        Platform.runLater(() -> {
            if (annotations.isEmpty()) {
                listView.getSelectionModel().clearSelection();
            }
            else if (annotations.size() == 1) {
                listView.getSelectionModel().select(annotations.iterator().next());
            }
            else {
                var allImages = new ArrayList<>(listView.getItems());
                var annos = new ArrayList<>(annotations);
                var selectionIndices = new ArrayList<Integer>();
                for (int i = 0; i < annos.size(); i++) {
                    var a = annos.get(i);
                    var j = allImages.indexOf(a);
                    if (j > -1) {
                        selectionIndices.add(j);
                    }
                }
                // to call selectIndices using var args we need head, tail args
                var head = selectionIndices.get(0);
                var tail = selectionIndices.subList(1, selectionIndices.size()).stream().mapToInt(Integer::intValue).toArray();
                listView.getSelectionModel().selectIndices(head, tail);
            }
        });
    }

    public ListView<Annotation> getListView() {
        return listView;
    }

    private void init() {
        listView = new ListView<>(FXCollections.observableArrayList());
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Annotation> call(ListView<Annotation> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Annotation item, boolean empty) {
                        Platform.runLater(() -> {
                            super.updateItem(item, empty);
                            getStyleClass().remove("ifx-localized-annotation");
                            if (item == null || empty) {
                                setText("");
                                setTooltip(null);
                            } else {
                                var s = item.getConcept();
                                setText(s);
                                setTooltip(new Tooltip(s));
                            }
                        });
                    }
                };
            }
        });

        listView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    if (newv != null) {
                        var items = new ArrayList<>(listView.getSelectionModel().getSelectedItems());
                        Selection<Collection<Annotation>> selection = new Selection<>(AnnotationListViewController.this, items);
                        onAnnotationSelection.accept(selection);
                    }
                });
    }
}