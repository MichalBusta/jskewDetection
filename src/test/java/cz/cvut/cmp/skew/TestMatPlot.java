package cz.cvut.cmp.skew;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.junit.Test;
import org.math.plot.Plot2DPanel;

public class TestMatPlot {

	@Test
	public void test2dPlot() {
		
		double[] x = new double[100]; // 1000 random numbers from a normal (Gaussian) statistical law
        double[] y = new double[100];
        for( int i = 0; i < x.length; i++ )
        {
        	x[i] = i * 0.5;
        	y[i] = Math.sin(x[i]);
        }
        
        Plot2DPanel plot = new Plot2DPanel();

        // add a line plot to the PlotPanel
        plot.addLinePlot("my plot", x, y);
        plot.setFixedBounds(0, 0, 10);
        plot.setFixedBounds(1, -10, 10);
        
        plot.getAxis(0).setLightLabels();
        
        JFrame frame = new JFrame("a plot panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JDialog dlg = new JDialog(frame, true);
        dlg.setContentPane(plot);
        dlg.pack();
        dlg.setVisible(true);
	}

        public static void main(String[] args) {
                TestMatPlot test = new TestMatPlot();
            test.test2dPlot();
        }

}
