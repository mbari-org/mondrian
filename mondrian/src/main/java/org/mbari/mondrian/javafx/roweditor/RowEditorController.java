package org.mbari.mondrian.javafx.roweditor;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.mbari.mondrian.Initializer;
import org.mbari.mondrian.ToolBox;
import org.mbari.mondrian.msg.commands.Command;
import org.mbari.mondrian.msg.commands.CreateAssociationCmd;
import org.mbari.mondrian.msg.commands.DeleteAssociationsCmd;
import org.mbari.mondrian.msg.commands.UpdateAssociationCmd;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

import java.util.*;

/**
 * @author Brian Schlining
 * @since 2017-06-30T07:57:00
 */
public class RowEditorController {
    private AssociationEditorPaneController associationController;
    private AnnotationEditorPaneController rowController;
    private Pane root;
    private volatile Annotation annotation;
    private final ToolBox toolBox = Initializer.getToolBox();

    public RowEditorController() {
        rowController = AnnotationEditorPaneController.newInstance();
        associationController = AssociationEditorPaneController.newInstance();
        initialize();
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
        Platform.runLater(() -> {
            rowController.setAnnotation(annotation);
            BorderPane rowPane = rowController.getRoot();
            GridPane associationPane = associationController.getRoot();
            ObservableList<Node> children = this.root.getChildren();
            children.remove(associationPane);
            if (!children.contains(rowPane)) {
                children.add(rowPane);
            }
        });
    }

    private void initialize() {
        BorderPane rowPane = rowController.getRoot();
        GridPane associationPane = associationController.getRoot();

        this.root = new Pane(rowPane);
        rowPane.prefWidthProperty().bind(root.widthProperty());
        rowPane.prefHeightProperty().bind(root.heightProperty());
        associationPane.prefWidthProperty().bind(root.widthProperty());
        associationPane.prefHeightProperty().bind(root.heightProperty());

        rowController.getAddButton().setOnAction(v -> {
            associationController.setTarget(annotation, null);
            this.root.getChildren().remove(rowPane);
            this.root.getChildren().add(associationPane);
            associationController.requestFocus();
        });

        // -- IF either the edit button is pressed or enter is hit when
        // an association is selected, start edit
        rowController.getEditButton().setOnAction(v -> {
            rowController.getSelectedAssociations()
                    .stream()
                    .findFirst()
                    .ifPresent(ass -> {
                        associationController.setTarget(annotation, ass);
                        this.root.getChildren().remove(rowPane);
                        this.root.getChildren().add(associationPane);
                        associationController.requestFocus();
                    });
        });

        rowController.getAssociationListView().addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER &&
                    rowController.getAssociationListView().getSelectionModel().getSelectedItem() != null ) {

                rowController.getSelectedAssociations()
                        .stream()
                        .findFirst()
                        .ifPresent(ass -> {
                            associationController.setTarget(annotation, ass);
                            this.root.getChildren().remove(rowPane);
                            this.root.getChildren().add(associationPane);
                            associationController.requestFocus();
                        });
            }
        });

        rowController.getRemoveButton().setOnAction(v -> {
            List<Association> selectedAssociations = new ArrayList<>(rowController.getSelectedAssociations());
            if (selectedAssociations.size() > 0) {
                var cmd = new DeleteAssociationsCmd(annotation.getObservationUuid(), selectedAssociations);
                toolBox.eventBus()
                        .publish(cmd);
            }
        });



        associationController.getAddButton().setOnAction(v -> doAction());
        associationController.getLinkValueTextField().setOnAction(v -> doAction());

        associationController.getCancelButton().setOnAction(v -> {
            this.root.getChildren().remove(associationPane);
            this.root.getChildren().add(rowPane);
//            rowController.requestFocus();
        });
    }

    public Pane getRoot() {
        return root;
    }

    protected void doAction() {
        BorderPane rowPane = rowController.getRoot();
        GridPane associationPane = associationController.getRoot();
        Association selectedAssociation = associationController.getSelectedAssociation();
        Optional<Association> opt = associationController.getCustomAssociation();
        if (opt.isPresent() && annotation != null) {
            Association customAssociation = opt.get();
            Command cmd;
            if (selectedAssociation == null) {
                // Create new association
                cmd = new CreateAssociationCmd(customAssociation, annotation);
            }
            else {
                // Update existing association
                Association a = new Association(selectedAssociation.getUuid(), customAssociation);
                cmd = new UpdateAssociationCmd(annotation.getObservationUuid(), selectedAssociation, a);
            }
            toolBox.eventBus().publish(cmd);
            this.root.getChildren().remove(associationPane);
            this.root.getChildren().add(rowPane);
        }
    }
}
