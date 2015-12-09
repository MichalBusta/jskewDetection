package cz.cvut.cmp.skew;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


/**
 * Created by J����� on 25. 11. 2015.
 */
public class TestVerticalDominant {

    class EstimResult {

        public EstimResult(double skewValue, double skewEstimate, String name) {
            this.skewValue = skewValue;
            this.skewEstimate = skewEstimate;
            this.name = name;
        }

        public double skewValue;
        public double skewEstimate;
        public String name;
    }


    List<EstimResult> results = new LinkedList<EstimResult>();
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
        SkewEstimator est = new VerticalDominant();

        Path dir = Paths.get("src/main/resources/google4");
        int a = 0;
        Random random = new Random();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{png}")) {
        	for (Path entry: stream) {
        		
        		// read the image
        		System.out.println(entry.toAbsolutePath());
        		Mat img = Highgui.imread(entry.toAbsolutePath().toString(), Highgui.IMREAD_GRAYSCALE);
                if (img.cols() == 0)
                    continue; //TODO !! fix this
                Imgproc.copyMakeBorder(img, img, 0, 0, img.rows(), img.rows(), Imgproc.BORDER_CONSTANT, new Scalar(255, 255, 255));
        		//get random angle
        		int randomAngle = random.nextInt(20);

        		// skew the image
        		Mat edited = new Mat();
        		SkewEstimator.skewImageWBG(img, edited, Math.toRadians(randomAngle));
                EstimResult res = new EstimResult(randomAngle, est.estimateSkew(edited), entry.toAbsolutePath().toString());
                results.add(res);

                System.out.println("Uhel: " + res.skewValue + "; Odhad: " + res.skewEstimate);

                if (Math.abs((res.skewEstimate + res.skewValue)) < 4) {
                    this.correctEstimations++;
        		}
        		else {
        			OCVUtils.showImage(img);
        			OCVUtils.showImage(edited);
        			Mat narrowed = new Mat();
                    SkewEstimator.skewImage(edited, narrowed, Math.toRadians(res.skewEstimate));
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

    public double calculateStandardDeviation() {
        double dispersion = 0;
        double mean;

        double sum = 0;
        for (int a = 0; a < results.size(); a++) {
            sum += Math.abs(results.get(a).skewEstimate - results.get(a).skewValue);
        }
        mean = sum / results.size();
        for (int a = 0; a < results.size(); a++) {
            dispersion += (((results.get(a).skewEstimate - results.get(a).skewValue) - mean) * ((results.get(a).skewEstimate - results.get(a).skewValue) - mean));
        }
        return Math.sqrt(dispersion) / results.size();
    }

    public static void main(String[] args) {
        TestVerticalDominant tvd = new TestVerticalDominant();
        tvd.paths = tvd.getFileNames("src/main/resources/google4");
        tvd.testImages(tvd.paths);
    }
}

