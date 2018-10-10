package gov.ornl.datatableview;

import gov.ornl.datatable.Column;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class AxisUnivariate extends Axis {
    public final static double DEFAULT_BAR_WIDTH = 20d;
    public final static double DEFAULT_NARROW_BAR_WIDTH = 6d;
    public final static double HOVER_TEXT_SIZE = 8d;

    protected Text hoverValueText;
    private double contextRegionHeight = 10;

    private Rectangle upperContextBar;
    private Rectangle lowerContextBar;
    private Rectangle axisBar;

    private Text minValueText;
    private Text maxValueText;

    private Column column;


    public AxisUnivariate(DataTableView dataTableView, Column column) {
        super(dataTableView, column.getName());

        this.column = column;

        minValueText = new Text();
        minValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minValueText.setSmooth(true);

        maxValueText = new Text();
        maxValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxValueText.setSmooth(true);

        axisBar = new Rectangle();
        axisBar.setStroke(Color.DARKGRAY);
        axisBar.setFill(Color.WHITESMOKE);
        axisBar.setWidth(DEFAULT_BAR_WIDTH);
        axisBar.setSmooth(true);
        axisBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        upperContextBar = new Rectangle();
        upperContextBar.setStroke(Color.DARKGRAY);
        upperContextBar.setFill(Color.WHITESMOKE);
        upperContextBar.setSmooth(true);
        upperContextBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        lowerContextBar = new Rectangle();
        lowerContextBar.setStroke(Color.DARKGRAY);
        lowerContextBar.setFill(Color.WHITESMOKE);
        lowerContextBar.setSmooth(true);
        lowerContextBar.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        hoverValueText = new Text();
        hoverValueText.setFont(new Font(HOVER_TEXT_SIZE));
        hoverValueText.setSmooth(true);
        hoverValueText.setVisible(false);
        hoverValueText.setFill(DEFAULT_TEXT_COLOR);
        hoverValueText.setTextOrigin(VPos.BOTTOM);
        hoverValueText.setMouseTransparent(true);
//        getDataTableView().getChildren().add(hoverValueText);

        getGraphicsGroup().getChildren().addAll(upperContextBar, lowerContextBar, axisBar, minValueText, maxValueText);
    }

    public Column getColumn() { return column; }

    @Override
    public void resize (double left, double top, double width, double height) {
        super.resize(left, top, width, height);

        double barTop = getTitleText().getLayoutBounds().getMaxY() + minValueText.getLayoutBounds().getHeight() + 4;
        double barBottom = getBounds().getMinY() + getBounds().getHeight() - maxValueText.getLayoutBounds().getHeight();
        double focusTop = barTop + contextRegionHeight;
        double focusBottom = barBottom - contextRegionHeight;

        minValueText.setX(getBounds().getMinX() + ((width - minValueText.getLayoutBounds().getWidth()) / 2.));
        minValueText.setY(barBottom + minValueText.getLayoutBounds().getHeight());

        maxValueText.setX(getBounds().getMinX() + ((width - maxValueText.getLayoutBounds().getWidth()) / 2.));
        maxValueText.setY(barTop - 4d);

        axisBar.setX(getCenterX() - (axisBar.getWidth() / 2.));
        axisBar.setY(focusTop);
        axisBar.setHeight(focusBottom - focusTop);

        upperContextBar.setX(axisBar.getX());
        upperContextBar.setWidth(axisBar.getWidth());
        upperContextBar.setY(barTop);
        upperContextBar.setHeight(contextRegionHeight);

        lowerContextBar.setX(axisBar.getX());
        lowerContextBar.setWidth(axisBar.getWidth());
        lowerContextBar.setY(focusBottom);
        lowerContextBar.setHeight(contextRegionHeight);
    }
}
