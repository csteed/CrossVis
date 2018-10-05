package gov.ornl.pcpview;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

/**
 * Created by csg on 10/21/16.
 */
public class TupleDrawingAnimationTimer extends AnimationTimer {
    public final static Logger log = Logger.getLogger(TupleDrawingAnimationTimer.class.getName());

    private Canvas canvas;
    private Color tupleColor;
    private ArrayBlockingQueue<PCPTuple> tupleQueue;
    private int maxTuplesPerFrame;
    private ArrayList<PCPUnivariateAxis> axisList;
    private BooleanProperty running;
    public long id;

    public TupleDrawingAnimationTimer (Canvas canvas, Collection<PCPTuple> tuples, ArrayList<PCPUnivariateAxis> axisList, Color tupleColor, int maxTuplesPerFrame) {
        id = System.currentTimeMillis();
        this.canvas = canvas;
        this.axisList = axisList;
        this.tupleColor = tupleColor;
        tupleQueue = new ArrayBlockingQueue<PCPTuple>(tuples.size());
        tupleQueue.addAll(tuples);
        this.maxTuplesPerFrame = maxTuplesPerFrame;
        running = new SimpleBooleanProperty(false);
    }

    public final boolean isRunning() { return running.get(); }

    public ReadOnlyBooleanProperty runningProperty() { return running; }

    @Override
    public void handle(long now) {
        canvas.getGraphicsContext2D().setStroke(tupleColor);

        for (int ituple = 0; ituple < maxTuplesPerFrame; ituple++) {
            PCPTuple pcpTuple = tupleQueue.poll();
            if (pcpTuple == null) {
                this.stop();
                break;
            } else {
                for (int i = 1; i < pcpTuple.getXPoints().length; i++) {
                    canvas.getGraphicsContext2D().strokeLine(axisList.get(i - 1).getBarRightX(), pcpTuple.getYPoints()[i - 1],
                            axisList.get(i).getBarLeftX(), pcpTuple.getYPoints()[i]);
                }
            }
        }
    }

    @Override
    public void start() {
        super.start();
        running.set(true);
    }

    @Override
    public void stop() {
        super.stop();
        running.set(false);
    }
}
