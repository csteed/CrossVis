package gov.ornl.datatableview;

import gov.ornl.datatable.*;
import gov.ornl.util.GraphicsUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.logging.Logger;

public class DoubleAxis extends UnivariateAxis {
    private static final Logger log = Logger.getLogger(DoubleAxis.class.getName());

    public final static Color DEFAULT_OVERALL_DISPERSION_FILL = DEFAULT_HISTOGRAM_FILL;
    public final static Color DEFAULT_QUERY_DISPERSION_FILL = DEFAULT_QUERY_HISTOGRAM_FILL;
    public final static Color DEFAULT_DISPERSION_STROKE = DEFAULT_HISTOGRAM_STROKE;
    public final static Color DEFAULT_OVERALL_TYPICAL_STROKE = Color.DARKBLUE.darker();
    public final static Color DEFAULT_QUERY_TYPICAL_STROKE = Color.DARKBLUE;

    private Color overallDispersionFill = DEFAULT_OVERALL_DISPERSION_FILL;
    private Color queryDispersionFill = DEFAULT_QUERY_DISPERSION_FILL;
    private Color dispersionStroke = DEFAULT_DISPERSION_STROKE;
    private Color overallTypicalStroke = DEFAULT_OVERALL_TYPICAL_STROKE;
    private Color queryTypicalStroke = DEFAULT_QUERY_TYPICAL_STROKE;

    // summary statistics objects
    private Group overallSummaryStatisticsGroup = new Group();
    private Rectangle overallDispersionRectangle;
    private Line overallTypicalLine;
    private Group querySummaryStatisticsGroup = new Group();
    private Rectangle queryDispersionRectangle;
    private Line queryTypicalLine;
    private Group nonquerySummaryStatisticsGroup = new Group();
    private Rectangle nonqueryDispersionRectangle;
    private Line nonqueryTypicalLine;

    // histogram bin rectangles
    private Group overallHistogramGroup = new Group();
    private ArrayList<Rectangle> overallHistogramRectangles = new ArrayList<>();
    private Group queryHistogramGroup = new Group();
    private ArrayList<Rectangle> queryHistogramRectangles = new ArrayList<>();

    private Text minValueText;
    private Text maxValueText;
    private Text minFocusValueText;
    private Text maxFocusValueText;

    private DoubleProperty minFocusValue;
    private DoubleProperty maxFocusValue;

    private DoubleAxisSelection draggingSelection;

    private double draggingMinFocusValue;
    private double draggingMaxFocusValue;
    private Text draggingContextValueText;
    private Line draggingContextLine;

    public DoubleAxis(DataTableView dataTableView, DoubleColumn column) {
        super(dataTableView, column);

        draggingContextLine = new Line();
        draggingContextLine.setStroke(getLowerContextBarHandle().getStroke());
        draggingContextLine.setStrokeWidth(getLowerContextBarHandle().getStrokeWidth());
        draggingContextLine.setStrokeLineCap(StrokeLineCap.ROUND);

        draggingContextValueText = new Text();
        draggingContextValueText.setFill(Color.BLACK);
        draggingContextValueText.setFont(Font.font(DEFAULT_TEXT_SIZE));

        minFocusValue = new SimpleDoubleProperty(column.getMinimumScaleValue());
        maxFocusValue = new SimpleDoubleProperty(column.getMaximumScaleValue());

        minValueText = new Text(String.valueOf(column.getMinimumScaleValue()));
        minValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minValueText.setSmooth(true);
        minValueText.setTextOrigin(VPos.TOP);
        minValueText.setTranslateY(1.);

        maxValueText = new Text(String.valueOf(column.getMaximumScaleValue()));
        maxValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxValueText.setSmooth(true);
        maxValueText.setTextOrigin(VPos.BOTTOM);
        maxValueText.setTranslateY(-2.);

        minFocusValueText = new Text();
        minFocusValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        minFocusValueText.setSmooth(true);
        minFocusValueText.setTextOrigin(VPos.TOP);
        minFocusValueText.setTranslateY(2.);
        minFocusValueText.setMouseTransparent(true);

        maxFocusValueText = new Text();
        maxFocusValueText.setFont(new Font(DEFAULT_TEXT_SIZE));
        maxFocusValueText.setSmooth(true);
        maxFocusValueText.setTextOrigin(VPos.BOTTOM);
        maxFocusValueText.setTranslateY(-3.);
        maxFocusValueText.setMouseTransparent(true);

        overallDispersionRectangle = new Rectangle();
        overallDispersionRectangle.setFill(overallDispersionFill);
        overallDispersionRectangle.setSmooth(true);
        overallDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        overallTypicalLine = makeLine();
        overallTypicalLine.setStroke(overallTypicalStroke);

        overallSummaryStatisticsGroup.getChildren().addAll(overallDispersionRectangle, overallTypicalLine);

        queryDispersionRectangle = new Rectangle();
        Color dispersionFill = new Color(dataTableView.getSelectedItemsColor().getRed(), dataTableView.getSelectedItemsColor().getGreen(),
                dataTableView.getSelectedItemsColor().getBlue(), 0.8);
        queryDispersionRectangle.setFill(dispersionFill);
        queryDispersionRectangle.setStroke(Color.DARKGRAY);
        queryDispersionRectangle.setSmooth(true);
        queryDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        queryTypicalLine = makeLine();
        queryTypicalLine.setStroke(queryTypicalStroke);

        querySummaryStatisticsGroup.getChildren().addAll(queryDispersionRectangle, queryTypicalLine);

        nonqueryDispersionRectangle = new Rectangle();
        dispersionFill = new Color(dataTableView.getUnselectedItemsColor().getRed(), dataTableView.getUnselectedItemsColor().getGreen(),
                dataTableView.getUnselectedItemsColor().getBlue(), 0.8);
        nonqueryDispersionRectangle.setFill(dispersionFill);
        nonqueryDispersionRectangle.setStroke(Color.DARKGRAY);
        nonqueryDispersionRectangle.setSmooth(true);
        nonqueryDispersionRectangle.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        nonqueryTypicalLine = makeLine();
        nonqueryTypicalLine.setStroke(queryTypicalStroke);

        nonquerySummaryStatisticsGroup.getChildren().addAll(nonqueryDispersionRectangle, nonqueryTypicalLine);

        overallSummaryStatisticsGroup.setMouseTransparent(true);
        querySummaryStatisticsGroup.setMouseTransparent(true);
        nonquerySummaryStatisticsGroup.setMouseTransparent(true);
        overallHistogramGroup.setMouseTransparent(true);
        queryHistogramGroup.setMouseTransparent(true);

        getGraphicsGroup().getChildren().add(0, overallHistogramGroup);
        getGraphicsGroup().getChildren().add(1, queryHistogramGroup);
        getGraphicsGroup().getChildren().addAll(minValueText,
                maxValueText, minFocusValueText, maxFocusValueText, overallSummaryStatisticsGroup,
                querySummaryStatisticsGroup, nonquerySummaryStatisticsGroup);

        registerListeners();
    }

    @Override
    protected Object getValueForAxisPosition(double axisPosition) {
        if (axisPosition < getFocusMaxPosition()) {
            return GraphicsUtil.mapValue(axisPosition, getFocusMaxPosition(), getUpperContextBar().getY(),
                    getMaxFocusValue(), doubleColumn().getMaximumScaleValue());
//            return GraphicsUtil.mapValue(axisPosition, getFocusMaxPosition(), getUpperContextBar().getY(),
//                    getMaxFocusValue(), doubleColumn().getStatistics().getMaxValue());
        } else if (axisPosition > getFocusMinPosition()) {
            return GraphicsUtil.mapValue(axisPosition, getFocusMinPosition(), getLowerContextBar().getY() + getLowerContextBar().getHeight(),
                    getMinFocusValue(), doubleColumn().getMinimumScaleValue());
//            return GraphicsUtil.mapValue(axisPosition, getFocusMinPosition(), getLowerContextBar().getY() + getLowerContextBar().getHeight(),
//                    getMinFocusValue(), doubleColumn().getStatistics().getMinValue());
        }

        return GraphicsUtil.mapValue(axisPosition, getFocusMinPosition(), getFocusMaxPosition(),
                getMinFocusValue(), getMaxFocusValue());

//        double value = GraphicsUtil.mapValue(axisPosition, getFocusMinPosition(), getFocusMaxPosition(),
//                doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue());
//        return value;
    }

    private double getAxisPositionForValue(double value) {
        if (value > getMaxFocusValue()) {
            return GraphicsUtil.mapValue(value, getMaxFocusValue(), doubleColumn().getMaximumScaleValue(),
                    getFocusMaxPosition(), getUpperContextBar().getY());
//            return GraphicsUtil.mapValue(value, getMaxFocusValue(), doubleColumn().getStatistics().getMaxValue(),
//                    getFocusMaxPosition(), getUpperContextBar().getY());
        } else if (value < getMinFocusValue()) {
            return GraphicsUtil.mapValue(value, getMinFocusValue(), doubleColumn().getMinimumScaleValue(),
                    getFocusMinPosition(), getLowerContextBar().getY() + getLowerContextBar().getHeight());
//            return GraphicsUtil.mapValue(value, getMinFocusValue(), doubleColumn().getStatistics().getMinValue(),
//                    getFocusMinPosition(), getLowerContextBar().getY() + getLowerContextBar().getHeight());
        }

        return GraphicsUtil.mapValue(value, getMinFocusValue(), getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());
//        double position = GraphicsUtil.mapValue(value, doubleColumn().getStatistics().getMinValue(),
//                doubleColumn().getStatistics().getMaxValue(), getFocusMinPosition(), getFocusMaxPosition());
//        return position;
    }

    public DoubleColumn doubleColumn () {
        return (DoubleColumn)getColumn();
    }

    public DoubleAxis doubleAxis() { return (DoubleAxis)this; }

    public String toString() {
        return doubleColumn().getName();
    }

    private void registerListeners() {
        doubleColumn().minimumScaleValueProperty().addListener(observable -> {
            minValueText.setText(String.valueOf(doubleColumn().getMinimumScaleValue()));
            if (getMinFocusValue() < doubleColumn().getMinimumScaleValue()) {
                setMinFocusValue(doubleColumn().getMinimumScaleValue());
            }
        });

        doubleColumn().maximumScaleValueProperty().addListener(observable -> {
            maxValueText.setText(String.valueOf(doubleColumn().getMaximumScaleValue()));
            if (getMaxFocusValue() > doubleColumn().getMaximumScaleValue()) {
                setMaxFocusValue(doubleColumn().getMaximumScaleValue());
            }
        });
//        minValueText.textProperty().bindBidirectional(((DoubleColumn)getColumn()).getStatistics().minValueProperty(),
//                new NumberStringConverter());
//
//        maxValueText.textProperty().bindBidirectional(((DoubleColumn)getColumn()).getStatistics().maxValueProperty(),
//                new NumberStringConverter());
//
//        doubleColumn().getStatistics().minValueProperty().addListener(observable -> {
//            if (getMinFocusValue() < doubleColumn().getStatistics().getMinValue()) {
//                setMinFocusValue(doubleColumn().getStatistics().getMinValue());
//            }
//        });
//
//        doubleColumn().getStatistics().maxValueProperty().addListener(observable -> {
//            if (getMaxFocusValue() > doubleColumn().getStatistics().getMaxValue()) {
//                setMaxFocusValue(doubleColumn().getStatistics().getMaxValue());
//            }
//        });

        minFocusValueText.textProperty().bind(Bindings.convert(minFocusValue));

        maxFocusValueText.textProperty().bind(Bindings.convert(maxFocusValue));

        getDataTableView().selectedItemsColorProperty().addListener((observable, oldValue, newValue) -> {
            Color dispersionFill = new Color(newValue.getRed(), newValue.getGreen(), newValue.getBlue(), 0.8);
            queryDispersionRectangle.setFill(dispersionFill);
        });

        getDataTableView().unselectedItemsColorProperty().addListener((observable, oldValue, newValue) -> {
            Color dispersionFill = new Color(newValue.getRed(), newValue.getGreen(), newValue.getBlue(), 0.8);
            nonqueryDispersionRectangle.setFill(dispersionFill);
        });

        getDataTableView().showHistogramsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {

                if (newValue) {
                    resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
                }

                overallHistogramGroup.setVisible(newValue);
                queryHistogramGroup.setVisible(newValue);

//                if (newValue) {
//                    getGraphicsGroup().getChildren().add(0, overallHistogramGroup);
//                    getGraphicsGroup().getChildren().add(1, queryHistogramGroup);
//                } else {
//                    getGraphicsGroup().getChildren().remove(overallHistogramGroup);
//                    getGraphicsGroup().getChildren().remove(queryHistogramGroup);
//                }

//                resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
            }
        });

        getDataTableView().showSummaryStatisticsProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                overallSummaryStatisticsGroup.setVisible(newValue);

                if (newValue) {
                    resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
                    querySummaryStatisticsGroup.setVisible(getDataTable().getCalculateQueryStatistics());
                    nonquerySummaryStatisticsGroup.setVisible(getDataTable().getCalculateNonQueryStatistics());
                } else {
                    querySummaryStatisticsGroup.setVisible(false);
                    nonquerySummaryStatisticsGroup.setVisible(false);
                }

//                resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());

//                if (!newValue) {
//                    getGraphicsGroup().getChildren().remove(overallSummaryStatisticsGroup);
//                    getGraphicsGroup().getChildren().remove(querySummaryStatisticsGroup);
//                    getGraphicsGroup().getChildren().remove(nonquerySummaryStatisticsGroup);
//                }
            }
        });

        getDataTableView().summaryStatisticsDisplayModeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue != oldValue) {
                // calculate graphics for new setting
                resize(getBounds().getMinX(), getBounds().getMinY(), getBounds().getWidth(), getBounds().getHeight());
            }
        });

        getUpperContextBarHandle().setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
                dragStartPoint = new Point2D(event.getX(), event.getY());
                draggingContextLine.setStartX(getUpperContextBarHandle().getStartX());
                draggingContextLine.setEndX(getUpperContextBarHandle().getEndX());
                draggingContextLine.setStartY(getUpperContextBarHandle().getStartY());
                draggingContextLine.setEndY(getUpperContextBarHandle().getEndY());
                draggingContextLine.setTranslateY(0);
                draggingContextValueText.setX(getCenterX());
                draggingContextValueText.setY(maxFocusValueText.getY());
                draggingContextValueText.setTextOrigin(VPos.BOTTOM);
                draggingContextValueText.setTranslateY(0);
                getGraphicsGroup().getChildren().addAll(draggingContextLine, draggingContextValueText);
                getUpperContextBarHandle().setVisible(false);
                maxFocusValueText.setVisible(false);
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());
            double dy = dragEndPoint.getY() - dragStartPoint.getY();

            double y = draggingContextLine.getStartY() + dy;

            double newMaxFocusValue;
            if (y < getFocusMaxPosition()) {
                // above the max position (use context range)
                newMaxFocusValue = GraphicsUtil.mapValue(y, getFocusMaxPosition(), getUpperContextBar().getY(),
                        getMaxFocusValue(), doubleColumn().getMaximumScaleValue());
                if (newMaxFocusValue > doubleColumn().getMaximumScaleValue()) {
                    newMaxFocusValue = doubleColumn().getMaximumScaleValue();
                }
                if (y < getUpperContextBar().getY()) {
//                    dy = getUpperContextBar().getY();
                    dy = getUpperContextBar().getY() - dragStartPoint.getY();
                }
            } else {
                // in focus region (use focus min / max)
                newMaxFocusValue = GraphicsUtil.mapValue(y, getFocusMaxPosition(), getFocusMinPosition(),
                        getMaxFocusValue(), getMinFocusValue());
                if (newMaxFocusValue < getMinFocusValue()) {
                    newMaxFocusValue = getMinFocusValue();
                    dy = getAxisBar().getHeight();
                }
            }

            draggingMaxFocusValue = newMaxFocusValue;
            draggingContextValueText.setText(String.valueOf(draggingMaxFocusValue));
            draggingContextValueText.setTranslateY(dy - 2);
            draggingContextValueText.setTranslateX(-draggingContextValueText.getLayoutBounds().getWidth() / 2.);

            draggingContextLine.setTranslateY(dy);
        });

        getLowerContextBarHandle().setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
                dragStartPoint = new Point2D(event.getX(), event.getY());
                draggingContextLine.setStartX(getLowerContextBarHandle().getStartX());
                draggingContextLine.setEndX(getLowerContextBarHandle().getEndX());
                draggingContextLine.setStartY(getLowerContextBarHandle().getStartY());
                draggingContextLine.setEndY(getLowerContextBarHandle().getEndY());
                draggingContextLine.setTranslateY(0);
                draggingContextValueText.setX(getCenterX());
                draggingContextValueText.setY(minFocusValueText.getY());
                draggingContextValueText.setTranslateY(0);
                draggingContextValueText.setTextOrigin(VPos.TOP);
                getGraphicsGroup().getChildren().addAll(draggingContextLine, draggingContextValueText);
                getLowerContextBarHandle().setVisible(false);
                minFocusValueText.setVisible(false);
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());
            double dy = dragEndPoint.getY() - dragStartPoint.getY();

            double y = draggingContextLine.getStartY() + dy;

            double newMinFocusValue;
            if (y > getFocusMinPosition()) {
                // below the min position (use context range)
                newMinFocusValue = GraphicsUtil.mapValue(y, getFocusMinPosition(),
                        getLowerContextBar().getLayoutBounds().getMaxY(), getMinFocusValue(),
                        doubleColumn().getMinimumScaleValue());
//                        doubleColumn().getStatistics().getMinValue());
//                log.info("newMinFocusValue = " + newMinFocusValue + "  y = " + y + "  dy = " + dy);
                if (newMinFocusValue < doubleColumn().getMinimumScaleValue()) {
                    newMinFocusValue = doubleColumn().getMinimumScaleValue();
//                    if (newMinFocusValue < doubleColumn().getStatistics().getMinValue()) {
//                    newMinFocusValue = doubleColumn().getStatistics().getMinValue();
//                    dy = getLowerContextBar().getHeight() + RANGE_VALUE_TEXT_HEIGHT;
                }
                if (y > getLowerContextBar().getLayoutBounds().getMaxY()) {
                    dy = getLowerContextBar().getLayoutBounds().getMaxY() - dragStartPoint.getY();
                }
            } else {
                // in focus region (use focus min / max)
                newMinFocusValue = GraphicsUtil.mapValue(y, getFocusMaxPosition(), getFocusMinPosition(),
                        getMaxFocusValue(), getMinFocusValue());
                if (newMinFocusValue > getMaxFocusValue()) {
                    newMinFocusValue = getMaxFocusValue();
                    dy = -getAxisBar().getHeight();
                }
            }

            draggingMinFocusValue = newMinFocusValue;
            draggingContextValueText.setText(String.valueOf(draggingMinFocusValue));
            draggingContextValueText.setTranslateY(dy + 2);
            draggingContextValueText.setTranslateX(-draggingContextValueText.getLayoutBounds().getWidth() / 2.);

            draggingContextLine.setTranslateY(dy);
        });

//        getLowerContextBar().setOnMouseDragged(event -> {
//            if (!dragging) {
//                dragging = true;
//                dragStartPoint = new Point2D(event.getX(), event.getY());
//                draggingContextLine.setStartX(getBarLeftX());
//                draggingContextLine.setEndX(getBarRightX());
//                draggingContextLine.setStartY(getFocusMinPosition());
//                draggingContextLine.setEndY(draggingContextLine.getStartY());
//                draggingContextLine.setTranslateY(0);
//                draggingContextValueText.setX(minFocusValueText.getX());
//                draggingContextValueText.setY(minFocusValueText.getY());
//                draggingContextValueText.setTranslateY(0);
//                getGraphicsGroup().getChildren().addAll(draggingContextLine, draggingContextValueText);
//            }
//
//            dragEndPoint = new Point2D(event.getX(), event.getY());
//            double dy = dragEndPoint.getY() - dragStartPoint.getY();
//
//            double y = draggingContextLine.getStartY() + dy;
//
//            double newMinFocusValue;
//            if (y > getFocusMinPosition()) {
//                // above the min position (use context range)
//                newMinFocusValue = GraphicsUtil.mapValue(y, getFocusMinPosition(),
//                        getLowerContextBar().getLayoutBounds().getMaxY(), getMinFocusValue(),
//                        doubleColumn().getMinimumScaleValue());
////                        doubleColumn().getStatistics().getMinValue());
//                if (newMinFocusValue < doubleColumn().getMinimumScaleValue()) {
//                    newMinFocusValue = doubleColumn().getMinimumScaleValue();
////                    if (newMinFocusValue < doubleColumn().getStatistics().getMinValue()) {
////                    newMinFocusValue = doubleColumn().getStatistics().getMinValue();
//                    dy = getUpperContextBar().getHeight();
//                }
//            } else {
//                // in focus region (use focus min / max)
//                newMinFocusValue = GraphicsUtil.mapValue(y, getFocusMaxPosition(), getFocusMinPosition(),
//                        getMaxFocusValue(), getMinFocusValue());
//                if (newMinFocusValue > getMaxFocusValue()) {
//                    newMinFocusValue = getMaxFocusValue();
//                    dy = -getAxisBar().getHeight();
//                }
//            }
//
//            draggingMinFocusValue = newMinFocusValue;
//            draggingContextValueText.setText(String.valueOf(draggingMinFocusValue));
//            draggingContextValueText.setTranslateY(dy);
//
//            draggingContextLine.setTranslateY(dy);
//        });

        getLowerContextBarHandle().setOnMouseReleased(event -> {
            if (dragging) {
                dragging = false;
                getGraphicsGroup().getChildren().removeAll(draggingContextValueText, draggingContextLine);
                getLowerContextBarHandle().setVisible(true);
                minFocusValueText.setVisible(true);
                if (getMinFocusValue() != draggingMinFocusValue) {
                    setMinFocusValue(draggingMinFocusValue);
                    getDataTableView().resizeView();

                    ArrayList<DoubleAxisSelection> selectionsToRemove = new ArrayList<>();
                    for (AxisSelection axisSelection : getAxisSelectionList()) {
                        DoubleAxisSelection doubleAxisSelection = (DoubleAxisSelection)axisSelection;
                        if (doubleAxisSelection.getDoubleColumnSelectionRange().getMinValue() < getMinFocusValue() ||
                                doubleAxisSelection.getDoubleColumnSelectionRange().getMaxValue() > getMaxFocusValue()) {
                            selectionsToRemove.add(doubleAxisSelection);
                        }
                    }

                    if (!selectionsToRemove.isEmpty()) {
                        for (DoubleAxisSelection doubleAxisSelection : selectionsToRemove) {
                            getDataTable().removeColumnSelectionFromActiveQuery(doubleAxisSelection.getColumnSelection());
                        }
                    }
                }
            }
        });

//        getLowerContextBar().setOnMouseReleased(event -> {
//            if (dragging) {
//                dragging = false;
//                getGraphicsGroup().getChildren().removeAll(draggingContextValueText, draggingContextLine);
//                if (getMinFocusValue() != draggingMinFocusValue) {
//                    setMinFocusValue(draggingMinFocusValue);
//                    getDataTableView().resizeView();
//
//                    ArrayList<DoubleAxisSelection> selectionsToRemove = new ArrayList<>();
//                    for (AxisSelection axisSelection : getAxisSelectionList()) {
//                        DoubleAxisSelection doubleAxisSelection = (DoubleAxisSelection)axisSelection;
//                        if (doubleAxisSelection.getDoubleColumnSelectionRange().getMinValue() < getMinFocusValue() ||
//                            doubleAxisSelection.getDoubleColumnSelectionRange().getMaxValue() > getMaxFocusValue()) {
//                            selectionsToRemove.add(doubleAxisSelection);
//                        }
//                    }
//
//                    if (!selectionsToRemove.isEmpty()) {
//                        for (DoubleAxisSelection doubleAxisSelection : selectionsToRemove) {
//                            getDataTable().removeColumnSelectionFromActiveQuery(doubleAxisSelection.getColumnSelection());
//                        }
//                    }
//                }
//            }
//        });

//        getUpperContextBar().setOnMouseDragged(event -> {
//            if (!dragging) {
//                dragging = true;
//                dragStartPoint = new Point2D(event.getX(), event.getY());
//                draggingContextLine.setStartX(getBarLeftX());
//                draggingContextLine.setEndX(getBarRightX());
//                draggingContextLine.setStartY(getFocusMaxPosition());
//                draggingContextLine.setEndY(draggingContextLine.getStartY());
//                draggingContextLine.setTranslateY(0);
//                draggingContextValueText.setX(maxFocusValueText.getX());
//                draggingContextValueText.setY(maxFocusValueText.getY());
//                draggingContextValueText.setTranslateY(0);
//                getGraphicsGroup().getChildren().addAll(draggingContextLine, draggingContextValueText);
//            }
//
//            dragEndPoint = new Point2D(event.getX(), event.getY());
//            double dy = dragEndPoint.getY() - dragStartPoint.getY();
//
//            double y = draggingContextLine.getStartY() + dy;
//
//            double newMaxFocusValue;
//            if (y < getFocusMaxPosition()) {
//                // above the max position (use context range)
//                newMaxFocusValue = GraphicsUtil.mapValue(y, getFocusMaxPosition(), getUpperContextBar().getY(),
//                        getMaxFocusValue(), doubleColumn().getMaximumScaleValue());
////                        doubleColumn().getStatistics().getMaxValue());
//                if (newMaxFocusValue > doubleColumn().getMaximumScaleValue()) {
//                    newMaxFocusValue = doubleColumn().getMaximumScaleValue();
////                if (newMaxFocusValue > doubleColumn().getStatistics().getMaxValue()) {
////                    newMaxFocusValue = doubleColumn().getStatistics().getMaxValue();
//                    dy = -getUpperContextBar().getHeight();
//                }
//            } else {
//                // in focus region (use focus min / max)
//                newMaxFocusValue = GraphicsUtil.mapValue(y, getFocusMaxPosition(), getFocusMinPosition(),
//                        getMaxFocusValue(), getMinFocusValue());
//                if (newMaxFocusValue < getMinFocusValue()) {
//                    newMaxFocusValue = getMinFocusValue();
//                    dy = getAxisBar().getHeight();
//                }
//            }
//
//            draggingMaxFocusValue = newMaxFocusValue;
//            draggingContextValueText.setText(String.valueOf(draggingMaxFocusValue));
//            draggingContextValueText.setTranslateY(dy);
//
//            draggingContextLine.setTranslateY(dy);
//        });

        getUpperContextBarHandle().setOnMouseReleased(event -> {
            if (dragging) {
                dragging = false;
                getGraphicsGroup().getChildren().removeAll(draggingContextValueText, draggingContextLine);
                getUpperContextBarHandle().setVisible(true);
                maxFocusValueText.setVisible(true);
                if (getMaxFocusValue() != draggingMaxFocusValue) {
                    setMaxFocusValue(draggingMaxFocusValue);
                    getDataTableView().resizeView();

                    ArrayList<DoubleAxisSelection> selectionsToRemove = new ArrayList<>();
                    for (AxisSelection axisSelection : getAxisSelectionList()) {
                        DoubleAxisSelection doubleAxisSelection = (DoubleAxisSelection)axisSelection;
                        if (doubleAxisSelection.getDoubleColumnSelectionRange().getMinValue() < getMinFocusValue() ||
                                doubleAxisSelection.getDoubleColumnSelectionRange().getMaxValue() > getMaxFocusValue()) {
                            selectionsToRemove.add(doubleAxisSelection);
                        }
                    }

                    if (!selectionsToRemove.isEmpty()) {
                        for (DoubleAxisSelection doubleAxisSelection : selectionsToRemove) {
                            getDataTable().removeColumnSelectionFromActiveQuery(doubleAxisSelection.getColumnSelection());
                        }
                    }
                }
            }
        });

//        getUpperContextBar().setOnMouseReleased(event -> {
//            if (dragging) {
//                dragging = false;
//                getGraphicsGroup().getChildren().removeAll(draggingContextValueText, draggingContextLine);
//                if (getMaxFocusValue() != draggingMaxFocusValue) {
//                    setMaxFocusValue(draggingMaxFocusValue);
//                    getDataTableView().resizeView();
//
//                    ArrayList<DoubleAxisSelection> selectionsToRemove = new ArrayList<>();
//                    for (AxisSelection axisSelection : getAxisSelectionList()) {
//                        DoubleAxisSelection doubleAxisSelection = (DoubleAxisSelection)axisSelection;
//                        if (doubleAxisSelection.getDoubleColumnSelectionRange().getMinValue() < getMinFocusValue() ||
//                                doubleAxisSelection.getDoubleColumnSelectionRange().getMaxValue() > getMaxFocusValue()) {
//                            selectionsToRemove.add(doubleAxisSelection);
//                        }
//                    }
//
//                    if (!selectionsToRemove.isEmpty()) {
//                        for (DoubleAxisSelection doubleAxisSelection : selectionsToRemove) {
//                            getDataTable().removeColumnSelectionFromActiveQuery(doubleAxisSelection.getColumnSelection());
//                        }
//                    }
//                }
//            }
//        });

        getAxisBar().setOnMousePressed(event -> {
            dragStartPoint = new Point2D(event.getX(), event.getY());
        });

        getAxisBar().setOnMouseDragged(event -> {
            if (!dragging) {
                dragging = true;
            }

            dragEndPoint = new Point2D(event.getX(), event.getY());

            double selectionMaxY = Math.min(dragStartPoint.getY(), dragEndPoint.getY());
            double selectionMinY = Math.max(dragStartPoint.getY(), dragEndPoint.getY());

            selectionMaxY = selectionMaxY < getFocusMaxPosition() ? getFocusMaxPosition() : selectionMaxY;
            selectionMinY = selectionMinY > getFocusMinPosition() ? getFocusMinPosition() : selectionMinY;

            double maxSelectionValue = GraphicsUtil.mapValue(selectionMaxY, getFocusMaxPosition(), getFocusMinPosition(),
                    getMaxFocusValue(), getMinFocusValue());
            double minSelectionValue = GraphicsUtil.mapValue(selectionMinY, getFocusMaxPosition(), getFocusMinPosition(),
                    getMaxFocusValue(), getMinFocusValue());
//            double maxSelectionValue = GraphicsUtil.mapValue(selectionMaxY, getFocusMaxPosition(), getFocusMinPosition(),
//                    doubleColumn().getStatistics().getMaxValue(), doubleColumn().getStatistics().getMinValue());
//            double minSelectionValue = GraphicsUtil.mapValue(selectionMinY, getFocusMaxPosition(), getFocusMinPosition(),
//                    doubleColumn().getStatistics().getMaxValue(), doubleColumn().getStatistics().getMinValue());

            if (draggingSelection == null) {
                DoubleColumnSelectionRange selectionRange = new DoubleColumnSelectionRange(doubleColumn(), minSelectionValue, maxSelectionValue);
                draggingSelection = new DoubleAxisSelection(this, selectionRange, selectionMinY, selectionMaxY);
                axisSelectionGraphicsGroup.getChildren().add(draggingSelection.getGraphicsGroup());
                axisSelectionGraphicsGroup.toFront();
            } else {
                draggingSelection.update(minSelectionValue, maxSelectionValue, selectionMinY, selectionMaxY);
            }
        });

        getAxisBar().setOnMouseReleased(event -> {
            if (draggingSelection != null) {
                axisSelectionGraphicsGroup.getChildren().remove(draggingSelection.getGraphicsGroup());
                getDataTable().addColumnSelectionRangeToActiveQuery(draggingSelection.getColumnSelection());
                dragging = false;
                draggingSelection = null;
            }
        });

        getAxisBar().setOnMouseEntered(event -> {
            hoverValueText.setVisible(true);
            hoverValueText.toFront();
        });

        getAxisBar().setOnMouseExited(event -> {
            hoverValueText.setVisible(false);
        });

        getAxisBar().setOnMouseMoved(event -> {
            Object value = getValueForAxisPosition(event.getY());
            if (value != null) {
                hoverValueText.setText(getValueForAxisPosition(event.getY()).toString());
                hoverValueText.setY(event.getY());
                hoverValueText.setX(getCenterX() - hoverValueText.getLayoutBounds().getWidth() / 2.);
            } else {
                hoverValueText.setText("");
            }
//            hoverValueText.toFront();
        });
    }

    public double getMaxFocusValue() { return maxFocusValue.get(); }

    public void setMaxFocusValue(double value) { maxFocusValue.set(value); }

    public DoubleProperty maxFocusValueProperty() { return maxFocusValue; }

    public double getMinFocusValue() { return minFocusValue.get(); }

    public void setMinFocusValue(double value) { minFocusValue.set(value); }

    public DoubleProperty minFocusValueProperty() { return minFocusValue; }

    @Override
    protected AxisSelection addAxisSelection(ColumnSelection columnSelection) {
        // see if an axis selection already exists for the column selection
        for (AxisSelection axisSelection : getAxisSelectionList()) {
            if (axisSelection.getColumnSelection() == columnSelection) {
                // an axis selection already exists for the given column selection so abort
                return null;
            }
        }

        DoubleColumnSelectionRange doubleColumnSelection = (DoubleColumnSelectionRange)columnSelection;

        double selectionMinValuePosition = getAxisPositionForValue(doubleColumnSelection.getMinValue());
        double selectionMaxValuePosition = getAxisPositionForValue(doubleColumnSelection.getMaxValue());

        DoubleAxisSelection newAxisSelection = new DoubleAxisSelection(this, doubleColumnSelection,
                selectionMinValuePosition, selectionMaxValuePosition);

        axisSelectionGraphicsGroup.getChildren().add(newAxisSelection.getGraphicsGroup());
        axisSelectionGraphicsGroup.toFront();

        getAxisSelectionList().add(newAxisSelection);

        return newAxisSelection;
    }

    public void resize(double left, double top, double width, double height) {
        if (getDataTableView().isShowingSummaryStatistics()) {
            getAxisBar().setWidth(DEFAULT_BAR_WIDTH);
        } else {
            getAxisBar().setWidth(DEFAULT_NARROW_BAR_WIDTH);
        }

        super.resize(left, top, width, height);

        minValueText.setX(getBounds().getMinX() + ((width - minValueText.getLayoutBounds().getWidth()) / 2.));
        minValueText.setY(getLowerContextBar().getY() + getLowerContextBar().getHeight());
//        minValueText.setY(getLowerContextBar().getLayoutBounds().getMaxY() + RANGE_VALUE_TEXT_HEIGHT - 2.);
//        minValueText.setY(getFocusMinPosition() + minValueText.getLayoutBounds().getHeight());
//        minValueText.setX(getBarRightX() + 2.);
//        minValueText.setY(getFocusMinPosition() + (minValueText.getLayoutBounds().getHeight() / 4.));

//        maxValueText.setX(getBarRightX() + 2.);
        maxValueText.setX(getBounds().getMinX() + ((width - maxValueText.getLayoutBounds().getWidth()) / 2.));
        maxValueText.setY(getUpperContextBar().getY());
//        maxValueText.setY(getUpperContextBar().getLayoutBounds().getMinY() - 2.);
//        maxValueText.setY(getFocusMaxPosition() - 4);
//        maxValueText.setY(getFocusMaxPosition() + (maxValueText.getLayoutBounds().getHeight() / 4.));

//        minFocusValueText.setX(getBarRightX() + 4.);
        minFocusValueText.setX(getBounds().getMinX() + ((width - minFocusValueText.getLayoutBounds().getWidth()) / 2));
//        minFocusValueText.setY(getFocusMinPosition() + minFocusValueText.getFont().getSize());
//        minFocusValueText.setY(getAxisBar().getY() + getAxisBar().getHeight() + RANGE_VALUE_TEXT_HEIGHT - 2.);
        minFocusValueText.setY(getAxisBar().getY() + getAxisBar().getHeight());

//        maxFocusValueText.setX(getBarRightX() + 4.);
        maxFocusValueText.setX(getBounds().getMinX() + ((width - maxFocusValueText.getLayoutBounds().getWidth()) / 2.));
        maxFocusValueText.setY(getAxisBar().getY());
//        maxFocusValueText.setY(getAxisBar().getY() - 2.);
//        maxFocusValueText.setY(getFocusMaxPosition());

        if (!getDataTable().isEmpty()) {
            if (getDataTableView().isShowingHistograms()) {
                DoubleHistogram histogram = doubleColumn().getStatistics().getHistogram();
                double binHeight = (getFocusMinPosition() - getFocusMaxPosition()) / histogram.getNumBins();

                overallHistogramRectangles.clear();
                overallHistogramGroup.getChildren().clear();

//                int numBins = histogram.getNumBins();
//                int maxBinIndex = 0;
//                if (getMaxFocusValue() != doubleColumn().getStatistics().getMaxValue()) {
//                    double binValueSize = (doubleColumn().getStatistics().getMaxValue() - doubleColumn().getStatistics().getMinValue()) /
//                            histogram.getNumBins();
//                    maxBinIndex = (int)Math.floor((doubleColumn().getStatistics().getMaxValue() - getMaxFocusValue()) / binValueSize);
//
//                }
//
//                int minBinIndex = histogram.getNumBins();
//                if (getMinFocusValue() != doubleColumn().getStatistics().getMinValue()) {
//                    double binValueSize = (doubleColumn().getStatistics().getMaxValue() - doubleColumn().getStatistics().getMinValue()) /
//                            histogram.getNumBins();
//                    minBinIndex = (int)Math.ceil((getMinFocusValue() - doubleColumn().getStatistics().getMinValue()) / binValueSize);
//                    minBinIndex = histogram.getNumBins() - minBinIndex;
//                }

//                for (int i = maxBinIndex; i < minBinIndex; i++) {
                for (int i = 0; i < histogram.getNumBins(); i++) {
                    if (histogram.getBinCount(i) > 0) {
                        double binLowerBound = histogram.getBinLowerBound(i);
                        double binUpperBound = histogram.getBinUpperBound(i);

                        if (!(binLowerBound < getMinFocusValue()) && !(binUpperBound > getMaxFocusValue())) {
                            double binLowerY = GraphicsUtil.mapValue(binLowerBound, getMinFocusValue(), getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());
                            double binUpperY = GraphicsUtil.mapValue(binUpperBound, getMinFocusValue(), getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());

//                        double y = getFocusMaxPosition() + ((histogram.getNumBins() - i - 1) * binHeight);
                            double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i),
                                    0, histogram.getMaxBinCount(),
                                    getAxisBar().getWidth() + 2, getAxisBar().getWidth() + 2 + maxHistogramBinWidth);
                            double x = getBounds().getMinX() + ((width - binWidth) / 2.);
                            Rectangle rectangle = new Rectangle(x, binUpperY, binWidth, binLowerY - binUpperY);
                            rectangle.setStroke(histogramFill.darker());
                            rectangle.setFill(histogramFill);
                            overallHistogramGroup.getChildren().add(rectangle);
                            overallHistogramRectangles.add(rectangle);
                        }

//                    double y = getFocusMaxPosition() + ((histogram.getNumBins() - i - 1) * binHeight);
//                    double binWidth = GraphicsUtil.mapValue(histogram.getBinCount(i),
//                            0, histogram.getMaxBinCount(),
//                            getAxisBar().getWidth() + 2, getAxisBar().getWidth() + 2 + maxHistogramBinWidth);
//                    double x = getBounds().getMinX() + ((width - binWidth) / 2.);
//                    Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
//                    rectangle.setStroke(histogramFill.darker());
//                    rectangle.setFill(histogramFill);
//                    overallHistogramGroup.getChildren().add(rectangle);
//                    overallHistogramRectangles.add(rectangle);
                    }
                }

                queryHistogramGroup.getChildren().clear();
                queryHistogramRectangles.clear();

                if (getDataTable().getActiveQuery().hasColumnSelections()) {
                    DoubleColumnSummaryStats queryColumnSummaryStats = (DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnQuerySummaryStats(getColumn());

                    if (queryColumnSummaryStats != null) {
                        if (getDataTable().getCalculateQueryStatistics()) {
                            DoubleHistogram queryHistogram = ((DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnQuerySummaryStats(doubleColumn())).getHistogram();

                            for (int i = 0; i < queryHistogram.getNumBins(); i++) {
                                if (queryHistogram.getBinCount(i) > 0) {
                                    double binLowerBound = queryHistogram.getBinLowerBound(i);
                                    double binUpperBound = queryHistogram.getBinUpperBound(i);

                                    if (binLowerBound >= getMinFocusValue() && binUpperBound <= getMaxFocusValue()) {
                                        double binLowerY = GraphicsUtil.mapValue(binLowerBound, getMinFocusValue(), getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());
                                        double binUpperY = GraphicsUtil.mapValue(binUpperBound, getMinFocusValue(), getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());

                                        double binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i),
                                                0, histogram.getMaxBinCount(),
                                                getAxisBar().getWidth() + 2, getAxisBar().getWidth() + 2 + maxHistogramBinWidth);
                                        double x = getBounds().getMinX() + ((width - binWidth) / 2.);
                                        Rectangle rectangle = new Rectangle(x, binUpperY, binWidth, binLowerY - binUpperY);
                                        rectangle.setStroke(queryHistogramFill.darker());
                                        rectangle.setFill(queryHistogramFill);

                                        queryHistogramRectangles.add(rectangle);
                                        queryHistogramGroup.getChildren().add(rectangle);
                                    }
                                }
                            }

//                            for (int i = 0; i < queryHistogram.getNumBins(); i++) {
//                                double y = getFocusMaxPosition() + ((queryHistogram.getNumBins() - i - 1) * binHeight);
//                                double binWidth = GraphicsUtil.mapValue(queryHistogram.getBinCount(i),
//                                        0, histogram.getMaxBinCount(),
//                                        getAxisBar().getWidth() + 2, getAxisBar().getWidth() + 2 + maxHistogramBinWidth);
//                                double x = getBounds().getMinX() + ((width - binWidth) / 2.);
//                                Rectangle rectangle = new Rectangle(x, y, binWidth, binHeight);
//                                rectangle.setStroke(queryHistogramFill.darker());
//                                rectangle.setFill(queryHistogramFill);
//
//                                queryHistogramRectangles.add(rectangle);
//                                queryHistogramGroup.getChildren().add(rectangle);
//                            }
                        }
                    }
                }
//            } else {
//                getGraphicsGroup().getChildren().removeAll(queryHistogramGroup, overallHistogramGroup);
            }

            if (getDataTableView().isShowingSummaryStatistics()) {
                double overallTypicalValue;
                double overallDispersionTopValue;
                double overallDispersionBottomValue;

                if (getDataTableView().getSummaryStatisticsDisplayMode() == DataTableView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
                    overallTypicalValue = doubleColumn().getStatistics().getMeanValue();
                    overallDispersionTopValue = doubleColumn().getStatistics().getMeanValue() + doubleColumn().getStatistics().getStandardDeviationValue();
                    overallDispersionBottomValue = doubleColumn().getStatistics().getMeanValue() - doubleColumn().getStatistics().getStandardDeviationValue();
                } else {
                    overallTypicalValue = doubleColumn().getStatistics().getMedianValue();
                    overallDispersionTopValue = doubleColumn().getStatistics().getPercentile75Value();
                    overallDispersionBottomValue = doubleColumn().getStatistics().getPercentile25Value();
                }

                if (!(overallTypicalValue < getMinFocusValue()) && !(overallTypicalValue > getMaxFocusValue())) {
                    double typicalValueY = GraphicsUtil.mapValue(overallTypicalValue,
                            getMinFocusValue(), getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());
//                    double typicalValueY = GraphicsUtil.mapValue(overallTypicalValue,
//                            doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue(),
//                            getFocusMinPosition(), getFocusMaxPosition());
                    overallTypicalLine.setStartX(doubleAxis().getBarLeftX());
                    overallTypicalLine.setEndX(doubleAxis().getBarRightX());
                    overallTypicalLine.setStartY(typicalValueY);
                    overallTypicalLine.setEndY(typicalValueY);
                    overallTypicalLine.setVisible(true);
                } else {
                    overallTypicalLine.setVisible(false);
                }

                if (!(overallDispersionBottomValue > getMaxFocusValue()) || !(overallDispersionTopValue < getMinFocusValue())) {
                    double overallDispersionTop = GraphicsUtil.mapValue(overallDispersionTopValue, getMinFocusValue(),
                            getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());
                    overallDispersionTop = overallDispersionTop < getFocusMaxPosition() ? getFocusMaxPosition() : overallDispersionTop;
                    double overallDispersionBottom = GraphicsUtil.mapValue(overallDispersionBottomValue, getMinFocusValue(),
                            getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());
                    overallDispersionBottom = overallDispersionBottom > getFocusMinPosition() ? getFocusMinPosition() : overallDispersionBottom;
                    overallDispersionRectangle.setX(overallTypicalLine.getStartX());
                    overallDispersionRectangle.setWidth(overallTypicalLine.getEndX() - overallTypicalLine.getStartX());
                    overallDispersionRectangle.setY(overallDispersionTop);
                    overallDispersionRectangle.setHeight(overallDispersionBottom - overallDispersionTop);
                    overallDispersionRectangle.setVisible(true);
                } else {
                    overallDispersionRectangle.setVisible(false);
                }

//                if (!getGraphicsGroup().getChildren().contains(overallSummaryStatisticsGroup)) {
//                    getGraphicsGroup().getChildren().add(overallSummaryStatisticsGroup);
//                }
                querySummaryStatisticsGroup.setVisible(false);
                nonquerySummaryStatisticsGroup.setVisible(false);

                if (getDataTable().getActiveQuery().hasColumnSelections()) {
                    DoubleColumnSummaryStats queryColumnSummaryStats = (DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnQuerySummaryStats(getColumn());

                    double queryDispersionRectangleWidth = doubleAxis().getAxisBar().getWidth() / 4.;
//                    double queryDispersionRectangleCenterOffset;
//                    double nonqueryDispersionRectangleCenterOffset;
//
//                    if (getDataTable().getCalculateNonQueryStatistics()) {
//                        queryDispersionRectangleCenterOffset = (doubleAxis().getAxisBar().getWidth() - queryDispersionRectangleWidth) / 2.;
//                    } else {
//                        queryDispersionRectangleCenterOffset = queryDispersionRectangleWidth / 2.;
//                    }

                    if (queryColumnSummaryStats != null) {
                        querySummaryStatisticsGroup.setVisible(true);
                        double queryTypicalValue;
                        double queryDispersionTopValue;
                        double queryDispersionBottomValue;

                        if (getDataTableView().getSummaryStatisticsDisplayMode() == DataTableView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
                            queryTypicalValue = queryColumnSummaryStats.getMeanValue();
                            queryDispersionTopValue = queryColumnSummaryStats.getMeanValue() + queryColumnSummaryStats.getStandardDeviationValue();
                            queryDispersionBottomValue = queryColumnSummaryStats.getMeanValue() - queryColumnSummaryStats.getStandardDeviationValue();
                        } else {
                            queryTypicalValue = queryColumnSummaryStats.getMedianValue();
                            queryDispersionTopValue = queryColumnSummaryStats.getPercentile75Value();
                            queryDispersionBottomValue = queryColumnSummaryStats.getPercentile25Value();
                        }

                        if (!(queryTypicalValue < getMinFocusValue()) && !(queryTypicalValue > getMaxFocusValue())) {
                            double typicalValueY = GraphicsUtil.mapValue(queryTypicalValue, getMinFocusValue(), getMaxFocusValue(),
//                                    doubleColumn().getStatistics().getMinValue(), doubleColumn().getStatistics().getMaxValue(),
                                    getFocusMinPosition(), getFocusMaxPosition());
                            queryTypicalLine.setStartX(getCenterX() - (queryDispersionRectangleWidth + 1));
                            queryTypicalLine.setEndX(queryTypicalLine.getStartX() + queryDispersionRectangleWidth);
                            queryTypicalLine.setStartY(typicalValueY);
                            queryTypicalLine.setEndY(typicalValueY);
                            queryTypicalLine.setVisible(true);
                        } else {
                            queryTypicalLine.setVisible(false);
                        }

                        if (!(queryDispersionBottomValue > getMaxFocusValue()) || !(queryDispersionTopValue < getMinFocusValue())) {
                            double queryDispersionTop = GraphicsUtil.mapValue(queryDispersionTopValue, getMinFocusValue(),
                                    getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());
                            queryDispersionTop = queryDispersionTop < getFocusMaxPosition() ? getFocusMaxPosition() : queryDispersionTop;
                            double queryDispersionBottom = GraphicsUtil.mapValue(queryDispersionBottomValue, getMinFocusValue(),
                                    getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());
                            queryDispersionBottom = queryDispersionBottom > getFocusMinPosition() ? getFocusMinPosition() : queryDispersionBottom;
                            queryDispersionRectangle.setX(queryTypicalLine.getStartX());
                            queryDispersionRectangle.setWidth(queryTypicalLine.getEndX() - queryTypicalLine.getStartX());
                            queryDispersionRectangle.setY(queryDispersionTop);
                            queryDispersionRectangle.setHeight(queryDispersionBottom - queryDispersionTop);
                            queryDispersionRectangle.setVisible(true);
                        } else {
                            queryDispersionRectangle.setVisible(false);
                        }

//                        if (!getGraphicsGroup().getChildren().contains(querySummaryStatisticsGroup)) {
//                            getGraphicsGroup().getChildren().add(querySummaryStatisticsGroup);
//                        }
//                    } else {
//                        getGraphicsGroup().getChildren().remove(querySummaryStatisticsGroup);
                    }

                    // draw nonquery statistics shapes
                    DoubleColumnSummaryStats nonqueryColumnSummaryStats = (DoubleColumnSummaryStats)getDataTable().getActiveQuery().getColumnNonquerySummaryStats(getColumn());

                    if (nonqueryColumnSummaryStats != null) {
                        nonquerySummaryStatisticsGroup.setVisible(true);
                        double nonqueryTypicalValue;
                        double nonqueryDispersionTopValue;
                        double nonqueryDispersionBottomValue;

                        if (getDataTableView().getSummaryStatisticsDisplayMode() == DataTableView.STATISTICS_DISPLAY_MODE.MEAN_BOXPLOT) {
                            nonqueryTypicalValue = nonqueryColumnSummaryStats.getMeanValue();
                            nonqueryDispersionTopValue = nonqueryColumnSummaryStats.getMeanValue() + nonqueryColumnSummaryStats.getStandardDeviationValue();
                            nonqueryDispersionBottomValue = nonqueryColumnSummaryStats.getMeanValue() - nonqueryColumnSummaryStats.getStandardDeviationValue();
                        } else {
                            nonqueryTypicalValue = nonqueryColumnSummaryStats.getMedianValue();
                            nonqueryDispersionTopValue = nonqueryColumnSummaryStats.getPercentile75Value();
                            nonqueryDispersionBottomValue = nonqueryColumnSummaryStats.getPercentile25Value();
                        }

                        if (!(nonqueryTypicalValue < getMinFocusValue()) && !(nonqueryTypicalValue > getMaxFocusValue())) {
                            double typicalValueY = GraphicsUtil.mapValue(nonqueryTypicalValue,
                                    getMinFocusValue(), getMaxFocusValue(),
                                    getFocusMinPosition(), getFocusMaxPosition());
                            nonqueryTypicalLine.setStartX(getCenterX() + 1);
                            nonqueryTypicalLine.setEndX(nonqueryTypicalLine.getStartX() + queryDispersionRectangleWidth);
                            nonqueryTypicalLine.setStartY(typicalValueY);
                            nonqueryTypicalLine.setEndY(typicalValueY);
                            nonqueryTypicalLine.setVisible(true);
                        } else {
                            nonqueryTypicalLine.setVisible(false);
                        }

                        if (!(nonqueryDispersionBottomValue > getMaxFocusValue()) || !(nonqueryDispersionBottomValue < getMinFocusValue())) {
                            double nonqueryDispersionTop = GraphicsUtil.mapValue(nonqueryDispersionTopValue, getMinFocusValue(),
                                    getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());
                            nonqueryDispersionTop = nonqueryDispersionTop < getFocusMaxPosition() ? getFocusMaxPosition() : nonqueryDispersionTop;
                            double nonqueryDispersionBottom = GraphicsUtil.mapValue(nonqueryDispersionBottomValue,
                                    getMinFocusValue(), getMaxFocusValue(), getFocusMinPosition(), getFocusMaxPosition());
                            nonqueryDispersionBottom = nonqueryDispersionBottom > getFocusMinPosition() ? getFocusMinPosition() : nonqueryDispersionBottom;

                            nonqueryDispersionRectangle.setX(nonqueryTypicalLine.getStartX());
                            nonqueryDispersionRectangle.setWidth(nonqueryTypicalLine.getEndX() - nonqueryTypicalLine.getStartX());
                            nonqueryDispersionRectangle.setY(nonqueryDispersionTop);
                            nonqueryDispersionRectangle.setHeight(nonqueryDispersionBottom - nonqueryDispersionTop);
                            nonqueryDispersionRectangle.setVisible(true);
                        } else {
                            nonqueryDispersionRectangle.setVisible(false);
                        }

//                        if (!getGraphicsGroup().getChildren().contains(nonquerySummaryStatisticsGroup)) {
//                            getGraphicsGroup().getChildren().add(nonquerySummaryStatisticsGroup);
//                        }
//                    } else {
//                        getGraphicsGroup().getChildren().remove(nonquerySummaryStatisticsGroup);
                    }

                    if (getGraphicsGroup().getChildren().contains(axisSelectionGraphicsGroup)) {
                        int idx = getGraphicsGroup().getChildren().indexOf(overallSummaryStatisticsGroup);
                        getGraphicsGroup().getChildren().remove(axisSelectionGraphicsGroup);
                        getGraphicsGroup().getChildren().add(idx+1, axisSelectionGraphicsGroup);
                    }
//                } else {
//                    querySummaryStatisticsGroup.setVisible(false);
//                    nonquerySummaryStatisticsGroup.setVisible(false);
//                    getGraphicsGroup().getChildren().removeAll(querySummaryStatisticsGroup, nonquerySummaryStatisticsGroup);
                }
            }
        }
    }
}
