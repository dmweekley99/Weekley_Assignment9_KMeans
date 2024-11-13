import java.io.*;
import java.util.*;

public class KMeans {

    // Iris class to represent each data point
    static class Iris {
        double petalLength;
        double petalWidth;
        double sepalLength;
        double sepalWidth;
        String species;

        public Iris(double petalLength, double petalWidth, double sepalLength, double sepalWidth, String species) {
            this.petalLength = petalLength;
            this.petalWidth = petalWidth;
            this.sepalLength = sepalLength;
            this.sepalWidth = sepalWidth;
            this.species = species;
        }
    }

    // Centroid class to represent the centroid of each cluster
    static class Centroid {
        double petalLength;
        double petalWidth;
        double sepalLength;
        double sepalWidth;

        public Centroid(double petalLength, double petalWidth, double sepalLength, double sepalWidth) {
            this.petalLength = petalLength;
            this.petalWidth = petalWidth;
            this.sepalLength = sepalLength;
            this.sepalWidth = sepalWidth;
        }

        // Overriding equals method to compare centroids
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Centroid centroid = (Centroid) obj;
            return Double.compare(centroid.petalLength, petalLength) == 0 &&
                    Double.compare(centroid.petalWidth, petalWidth) == 0 &&
                    Double.compare(centroid.sepalLength, sepalLength) == 0 &&
                    Double.compare(centroid.sepalWidth, sepalWidth) == 0;
        }
    }

    // Method to calculate the Euclidean distance between an iris and a centroid
    public static double calculateDistance(Iris iris, Centroid centroid) {
        return Math.sqrt(Math.pow(iris.petalLength - centroid.petalLength, 2) +
                Math.pow(iris.petalWidth - centroid.petalWidth, 2) +
                Math.pow(iris.sepalLength - centroid.sepalLength, 2) +
                Math.pow(iris.sepalWidth - centroid.sepalWidth, 2));
    }

    // Method to read the iris data from a file
    public static List<Iris> readData(String fileName) {
        List<Iris> irisData = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                double petalLength = Double.parseDouble(values[0]);
                double petalWidth = Double.parseDouble(values[1]);
                double sepalLength = Double.parseDouble(values[2]);
                double sepalWidth = Double.parseDouble(values[3]);
                String species = values[4];

                irisData.add(new Iris(petalLength, petalWidth, sepalLength, sepalWidth, species));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return irisData;
    }

    // Method to randomly initialize centroids
    public static List<Centroid> initializeCentroids(List<Iris> data, int k) {
        Random rand = new Random();
        List<Centroid> centroids = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            Iris iris = data.get(rand.nextInt(data.size())); // Randomly choose an iris as a centroid
            centroids.add(new Centroid(iris.petalLength, iris.petalWidth, iris.sepalLength, iris.sepalWidth));
        }
        return centroids;
    }

    // Method to assign irises to the closest centroid
    public static List<List<Iris>> assignToClusters(List<Iris> data, List<Centroid> centroids) {
        List<List<Iris>> clusters = new ArrayList<>();
        for (int i = 0; i < centroids.size(); i++) {
            clusters.add(new ArrayList<>());
        }

        for (Iris iris : data) {
            double minDistance = Double.MAX_VALUE;
            int clusterIndex = 0;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = calculateDistance(iris, centroids.get(i));
                if (distance < minDistance) {
                    minDistance = distance;
                    clusterIndex = i;
                }
            }
            clusters.get(clusterIndex).add(iris);
        }
        return clusters;
    }

    // Method to update centroids based on the average of the points in each cluster
    public static List<Centroid> updateCentroids(List<List<Iris>> clusters) {
        List<Centroid> newCentroids = new ArrayList<>();
        for (List<Iris> cluster : clusters) {
            double sumPetalLength = 0, sumPetalWidth = 0, sumSepalLength = 0, sumSepalWidth = 0;
            for (Iris iris : cluster) {
                sumPetalLength += iris.petalLength;
                sumPetalWidth += iris.petalWidth;
                sumSepalLength += iris.sepalLength;
                sumSepalWidth += iris.sepalWidth;
            }
            int size = cluster.size();
            newCentroids.add(new Centroid(sumPetalLength / size, sumPetalWidth / size, sumSepalLength / size, sumSepalWidth / size));
        }
        return newCentroids;
    }

    // Method to output clustering results to a CSV file
    public static void outputResultsToFile(String fileName, List<List<Iris>> clusters) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            // Write the header (Cluster, Setosa, Versicolor, Virginica, Total)
            writer.write("Cluster,Setosa,Versicolor,Virginica,Total\n");

            int[] speciesTotals = new int[3]; // To hold the total counts for Setosa, Versicolor, and Virginica

            // Loop through each cluster and count the species
            for (int i = 0; i < clusters.size(); i++) {
                List<Iris> cluster = clusters.get(i);
                Map<String, Integer> speciesCount = new HashMap<>();
                for (Iris iris : cluster) {
                    speciesCount.put(iris.species, speciesCount.getOrDefault(iris.species, 0) + 1);
                }

                // Prepare the output for each cluster
                StringBuilder row = new StringBuilder();
                row.append("Cluster " + (i + 1)); // Cluster label

                int totalInCluster = 0;
                for (String species : new String[]{"Iris-setosa", "Iris-versicolor", "Iris-virginica"}) {
                    int speciesCountInCluster = speciesCount.getOrDefault(species, 0);
                    row.append(",").append(speciesCountInCluster); // Species count in cluster
                    speciesTotals[Arrays.asList("Iris-setosa", "Iris-versicolor", "Iris-virginica").indexOf(species)] += speciesCountInCluster;
                    totalInCluster += speciesCountInCluster;
                }

                row.append(",").append(totalInCluster); // Total for the current cluster
                writer.write(row.toString() + "\n");
            }

            // Write the totals row
            StringBuilder totalRow = new StringBuilder();
            totalRow.append("Total");
            int grandTotal = 0;
            for (int i = 0; i < speciesTotals.length; i++) {
                totalRow.append(",").append(speciesTotals[i]);
                grandTotal += speciesTotals[i];
            }
            totalRow.append(",").append(grandTotal); // Total for all clusters
            writer.write(totalRow.toString() + "\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        List<Iris> data = readData("./iris.txt");  // Read the iris dataset from file
        int k = 3;  // Number of clusters
        List<Centroid> centroids = initializeCentroids(data, k);

        boolean centroidsChanged;
        List<List<Iris>> clusters = new ArrayList<>();
        do {
            clusters = assignToClusters(data, centroids);  // Assign to clusters
            List<Centroid> newCentroids = updateCentroids(clusters);  // Update centroids

            centroidsChanged = false;
            for (int i = 0; i < centroids.size(); i++) {
                if (!centroids.get(i).equals(newCentroids.get(i))) {
                    centroidsChanged = true;
                    break;
                }
            }

            centroids = newCentroids;
        } while (centroidsChanged);

        // Write the results to a CSV file
        outputResultsToFile("clustering_results.csv", clusters);
        System.out.println("Clustering results have been written to 'clustering_results.csv'.");
    }
}
