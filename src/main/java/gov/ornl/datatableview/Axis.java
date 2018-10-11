package gov.ornl.datatableview;

import gov.ornl.datatable.DataTable;
import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class Axis {
    public final static double DEFAULT_TITLE_TEXT_SIZE = 12d;
    public final static double DEFAULT_TEXT_SIZE = 10d;
    public final static double DEFAULT_STROKE_WIDTH = 1.5;
    public final static Color DEFAULT_TEXT_COLOR = Color.BLACK;

    private StringProperty title;
    private DataTableView dataTableView;

    private Text titleText;
    private Rectangle titleTextRectangle;

    private Bounds bounds;
    private double centerX = 0d;
    private double centerY = 0d;

    private ObjectProperty<Color> textColor = new SimpleObjectProperty<>(DEFAULT_TEXT_COLOR);
    private BooleanProperty highlighted = new SimpleBooleanProperty(false);

    private Group graphicsGroup = new Group();

    // dragging variables
    protected Group axisDraggingGraphicsGroup;
    protected Point2D dragStartPoint;
    protected Point2D dragEndPoint;
    protected boolean dragging = false;

    public Axis(DataTableView dataTableView, String title) {
        this.dataTableView = dataTableView;
        this.title = new SimpleStringProperty(title);

        titleText = new Text(title);
        titleText.setFont(new Font(DEFAULT_TITLE_TEXT_SIZE));
        titleText.setSmooth(true);
        titleText.setFill(DEFAULT_TEXT_COLOR);
        titleText.setMouseTransparent(true);

        titleTextRectangle = new Rectangle();
        titleTextRectangle.setStrokeWidth(3.);
        titleTextRectangle.setStroke(Color.TRANSPARENT);
        titleTextRectangle.setFill(Color.TRANSPARENT);
        titleTextRectangle.setArcWidth(6.);
        titleTextRectangle.setArcHeight(6.);

        graphicsGroup.getChildren().addAll(titleTextRectangle, titleText);

        registerListeners();
    }

    public boolean isHighlighted() { return highlighted.get(); }

    public void setHighlighted(boolean highlighted) {
        if (isHighlighted() != highlighted) {
            this.highlighted.set(highlighted);
        }
    }

    public BooleanProperty highlightedProperty() { return highlighted; }

    public Color getTextColor() { return textColor.get(); }

    public void setTextColor(Color c) { textColor.set(c); }

    public ObjectProperty<Color> textColorProperty() { return textColor; }

    protected Bounds getBounds() { return bounds; }

    protected double getCenterX() { return centerX; }

    protected double getCenterY() { return centerY; }

    private void registerListeners() {
        titleText.textProperty().addListener((observable, oldValue, newValue) -> {
            titleText.setX(bounds.getMinX() + ((bounds.getWidth() - titleText.getLayoutBounds().getWidth()) / 2.));
            titleText.setY(bounds.getMinY() + titleText.getLayoutBounds().getHeight());
        });

        titleTextRectangle.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                if (!isHighlighted()) {
                    dataTableView.setHighlightedAxis(this);
                } else {
                    dataTableView.setHighlightedAxis(null);
                }
            }
        });

        titleTextRectangle.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                final ContextMenu contextMenu = new ContextMenu();
                MenuItem hideMenuItem = new MenuItem("Remove Axis");
                MenuItem closeMenuItem = new MenuItem("Close Menu");
                contextMenu.getItems().addAll(hideMenuItem, closeMenuItem);
                hideMenuItem.setOnAction(removeEvent -> {
                    // remove this axis from the data table view
                    dataTableView.removeAxis(this);
//                    getDataTable().disableColumn(column);
                });
                closeMenuItem.setOnAction(closeEvent -> contextMenu.hide());
                contextMenu.show(dataTableView, event.getScreenX(), event.getScreenY());
            }
        });

        titleTextRectangle.setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
                makeAxisDraggingGraphics();
                axisDraggingGraphicsGroup.setEffect(new DropShadow());
                dataTableView.getPane().getChildren().add(axisDraggingGraphicsGroup);
                dragStartPoint = new Point2D(event.getX(), event.getY());
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());
            axisDraggingGraphicsGroup.setTranslateX(event.getX() - dragStartPoint.getX());
        });

        titleTextRectangle.setOnMouseReleased(event -> {
            if (dragging) {
                dataTableView.getPane().getChildren().remove(axisDraggingGraphicsGroup);
                dragging = false;
                int newAxisPosition = (int)dragEndPoint.getX() / dataTableView.getAxisSpacing();
                dataTableView.setAxisPosition(this, newAxisPosition);
            }
        });

        highlighted.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                titleTextRectangle.setFill(Color.web("#ffc14d"));
            } else {
                titleTextRectangle.setFill(Color.TRANSPARENT);
            }
        });
    }

    protected void makeAxisDraggingGraphics() {
        Text draggingNameText = new Text(titleText.getText());
        draggingNameText.setX(titleText.getX());
        draggingNameText.setY(titleText.getY());
        draggingNameText.setFont(titleText.getFont());

        axisDraggingGraphicsGroup = new Group(draggingNameText);
        axisDraggingGraphicsGroup.setTranslateY(5);

        if (this instanceof UnivariateAxis) {
            UnivariateAxis univariateAxis = (UnivariateAxis)this;
            Rectangle dragAxisBar = new Rectangle(univariateAxis.getAxisBar().getX(),
                    univariateAxis.getAxisBar().getY(), univariateAxis.getAxisBar().getWidth(),
                    univariateAxis.getAxisBar().getHeight());
            dragAxisBar.setStroke(univariateAxis.getAxisBar().getStroke());
            dragAxisBar.setFill(univariateAxis.getAxisBar().getFill());

            Rectangle dragUpperContextBar = new Rectangle(univariateAxis.getUpperContextBar().getX(),
                    univariateAxis.getUpperContextBar().getY(),
                    univariateAxis.getUpperContextBar().getWidth(),
                    univariateAxis.getUpperContextBar().getHeight());
            dragUpperContextBar.setStroke(univariateAxis.getUpperContextBar().getStroke());
            dragUpperContextBar.setFill(univariateAxis.getUpperContextBar().getFill());

            Rectangle dragLowerContextBar = new Rectangle(univariateAxis.getLowerContextBar().getX(),
                    univariateAxis.getLowerContextBar().getY(),
                    univariateAxis.getLowerContextBar().getWidth(), univariateAxis.getLowerContextBar().getHeight());
            dragLowerContextBar.setStroke(univariateAxis.getLowerContextBar().getStroke());
            dragLowerContextBar.setFill(univariateAxis.getLowerContextBar().getFill());

            axisDraggingGraphicsGroup.getChildren().addAll(dragUpperContextBar, dragLowerContextBar, dragAxisBar);
        } else if (this instanceof BivariateAxis) {
            BivariateAxis biAxis = (BivariateAxis)this;

            Rectangle scatterplotRectangle = new Rectangle(biAxis.getScatterplotRectangle().getX(),
                    biAxis.getScatterplotRectangle().getY(), biAxis.getScatterplotRectangle().getWidth(),
                    biAxis.getScatterplotRectangle().getHeight());
            scatterplotRectangle.setFill(dataTableView.getBackgroundColor());
            scatterplotRectangle.setStroke(biAxis.getScatterplotRectangle().getStroke());

            axisDraggingGraphicsGroup.getChildren().add(scatterplotRectangle);
        }
    }

    public Group getGraphicsGroup() { return graphicsGroup; }

    protected DataTableView getDataTableView() { return dataTableView; }

    protected DataTable getDataTable() { return dataTableView.getDataTable(); }

    protected Text getTitleText() { return titleText; }

    public void resize (double left, double top, double width, double height) {
        bounds = new BoundingBox(left, top, width, height);
        centerX = left + (width / 2.);
        centerY = top + (height / 2.);

        titleText.setText(title.getValue());
        if (titleText.getLayoutBounds().getWidth() > bounds.getWidth()) {
            // truncate the column name to fit axis bounds
            while (titleText.getLayoutBounds().getWidth() > bounds.getWidth()) {
                titleText.setText(titleText.getText().substring(0, titleText.getText().length() - 1));
            }
        }
        titleText.setX(bounds.getMinX() + ((width - titleText.getLayoutBounds().getWidth()) / 2.));
        titleText.setY(bounds.getMinY() + titleText.getLayoutBounds().getHeight());

        titleTextRectangle.setX(titleText.getX() - 4.);
        titleTextRectangle.setY(titleText.getY() - titleText.getLayoutBounds().getHeight());
        titleTextRectangle.setWidth(titleText.getLayoutBounds().getWidth() + 8.);
        titleTextRectangle.setHeight(titleText.getLayoutBounds().getHeight() + 4.);
    }
}
