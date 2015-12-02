package cz.cvut.cmp.skew;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 * Created by J����� on 25. 11. 2015.
 */
public class TestVerticalDominant {
    double[] skewValue;
    double[] skewEstimate;
    int[] difference; // (skewValue-SkewEstimate)
    String[] paths; // paths to the test images
    int totalImageNumber;
    int correctEstimations;
    double SuccessRate; // correctEstimations percentage
    double stDev; // the standard deviation of the estimations


    // gets the path to every png image of the given directory
    public String[] getFileNames(String directory) {
        String[] fileNames;
        File f;
        int NumberOfFiles = 0;

        try {
            // create new file
            f = new File(directory);

            // array of files, add checking for directories etc.!
            paths = f.list();

            // for each name in the path array
            for (String path : paths) {
                // get the number of png files so that we can create the array of paths
                if (path.endsWith(".png")) {
                    NumberOfFiles++;
                }

            }
            // save the path names into the array
            fileNames = new String[NumberOfFiles];
            int a = 0;
            for (String path : paths) {
                if (path.endsWith(".png")) {
                    fileNames[a] = path;
                    a++;
                }

            }
            return fileNames;
        } catch (Exception e) {
            // if any error occurs
            e.printStackTrace();
        }
        fileNames = new String[0];
        return fileNames;
    }
    
    // test the estimator using random skew values
    public void testImages(String[] paths) {
        correctEstimations = 0;
        skewValue = new double[paths.length];
        skewEstimate = new double[paths.length];
        SkewEstimator est = new VerticalDominant();
        
        Path dir  = Paths.get("/textspotter/SkewDetection/google4");
        int a = 0;
        Random random = new Random();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{png}")) {
        	for (Path entry: stream) {
        		
        		// read the image
        		System.out.println(entry.toAbsolutePath());
        		Mat img = Highgui.imread(entry.toAbsolutePath().toString(), Highgui.IMREAD_GRAYSCALE);
        		Imgproc.copyMakeBorder(img, img, 0, 0, img.rows(), img.rows(), Imgproc.BORDER_CONSTANT, new Scalar(255, 255, 255));
        		//get random angle
        		int randomAngle = random.nextInt(20);
        		skewValue[a] = randomAngle;

        		// skew the image
        		Mat edited = new Mat();
        		SkewEstimator.skewImageWBG(img, edited, Math.toRadians(randomAngle));

        		// estimate the skew
        		skewEstimate[a] = est.estimateSkew(edited);
        		System.out.println("Uhel: " + skewValue[a] + "; Odhad: " + skewEstimate[a]);

        		if ( Math.abs((skewEstimate[a] + randomAngle)) < 4 ){
        			this.correctEstimations++;
        		}
        		else {
        			OCVUtils.showImage(img);
        			OCVUtils.showImage(edited);
        			Mat narrowed = new Mat();
        			SkewEstimator.skewImage(edited, narrowed, Math.toRadians(skewEstimate[a]));
        			OCVUtils.showImage(narrowed);
        		}
        		System.out.println(String.format("Recall: {%f}", this.correctEstimations / new Float(a)));
        		a++;
        		
        	}
        } catch (DirectoryIteratorException e) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw new RuntimeException(e);
        } catch (IOException e1) {
        	throw new RuntimeException(e1);
		}
    }

    public void calculateStandardDeviation() {
    	
    }

    public static void main(String[] args) {
        TestVerticalDominant tvd = new TestVerticalDominant();
        tvd.paths = tvd.getFileNames("/textspotter/SkewDetection/google4");
        tvd.testImages(tvd.paths);
    }
}

