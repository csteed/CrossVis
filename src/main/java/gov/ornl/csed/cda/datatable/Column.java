package gov.ornl.csed.cda.datatable;

import javafx.beans.property.*;

import java.io.Serializable;

public class Column implements Serializable {

    // TODO: Remove the summary stats objects and fully adopt the bean property model for stats
    private SummaryStats summaryStats = new SummaryStats();
    private SummaryStats focusSummaryStats = null;

    private boolean isDiscrete = false;

    private StringProperty name;
    private BooleanProperty enabled;
    private DoubleProperty meanValue;
    private DoubleProperty minValue;
    private DoubleProperty maxValue;
    private DoubleProperty standardDeviationValue;

    public Column(String name) {
        this.name = new SimpleStringProperty(name);
        enabled = new SimpleBooleanProperty(true);
        meanValue = new SimpleDoubleProperty(Double.NaN);
        minValue = new SimpleDoubleProperty(Double.NaN);
        maxValue = new SimpleDoubleProperty(Double.NaN);
        standardDeviationValue = new SimpleDoubleProperty(Double.NaN);
    }

    public SummaryStats getFocusSummaryStats () {
        return focusSummaryStats;
    }

    public void makeDiscrete() {
        isDiscrete = true;
    }

    public void makeContinuous() {
        isDiscrete = false;
    }

    public boolean isContinuous () {
        return !isDiscrete;
    }

    public boolean isDiscrete() {
        return isDiscrete;
    }

    public void setFocusSummaryStats (SummaryStats focusSummaryStats) {
        this.focusSummaryStats = focusSummaryStats;
    }

    public SummaryStats getSummaryStats() { return summaryStats; }

    public void setSummaryStats(SummaryStats summaryStats) {
        this.summaryStats = summaryStats;
    }

    public final boolean getEnabled() { return enabled.get(); }

    public final void setEnabled (boolean enabled) { this.enabled.set(enabled); }

    public BooleanProperty enabledProperty() { return enabled; }

    public final String getName() { return name.get(); }

    public final void setName(String name) {
        this.name.setValue(name);
    }

    public StringProperty nameProperty() { return name; }

    public final double getStandardDeviationValue() { return standardDeviationValue.get(); }

    public DoubleProperty standardDeviationValueProperty() { return standardDeviationValue; }

    public final double getMeanValue() { return meanValue.get(); }

    public DoubleProperty meanValueProperty() { return meanValue; }

    public final double getMinValue() { return minValue.get(); }

    public DoubleProperty minValueProperty() { return minValue; }

    public final double getMaxValue() { return maxValue.get(); }

    public DoubleProperty maxValueProperty() { return maxValue; }

    public String toString() {
        return getName();
    }
}
