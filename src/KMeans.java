import java.io.*;
import java.util.*;

public class KMeans {

    // Iris class to represent each data point (iris observation)
    static class Iris {
        double petalLength;  // Petal length of the iris
        double petalWidth;   // Petal width of the iris
        double sepalLength;  // Sepal length of the iris
        double sepalWidth;   // Sepal width of the iris
        String species;      // Species of the iris (i.e., Iris-setosa, Iris-versicolor)

        // Constructor to initialize an Iris object with its attributes
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
        double petalLength;  // Petal length of the centroid
        double petalWidth;   // Petal width of the centroid
        double sepalLength;  // Sepal length of the centroid
        double sepalWidth;   // Sepal width of the centroid

        // Constructor to initialize a Centroid object with its attributes
        public Centroid(double petalLength, double petalWidth, double sepalLength, double sepalWidth) {
            this.petalLength = petalLength;
            this.petalWidth = petalWidth;
            this.sepalLength = sepalLength;
            this.sepalWidth = sepalWidth;
        }

        // Overriding the equals method to compare two centroids based on their attributes
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

    // Method to calculate the Euclidean distance between an Iris object and a Centroid
    public static double calculateDistance(Iris iris, Centroid centroid) {
        return Math.sqrt(Math.pow(iris.petalLength - centroid.petalLength, 2) +
                Math.pow(iris.petalWidth - centroid.petalWidth, 2) +
                Math.pow(iris.sepalLength - centroid.sepalLength, 2) +
                Math.pow(iris.sepalWidth - centroid.sepalWidth, 2));
    }

    // Method to read the iris dataset from a file
    public static List<Iris> readData(String fileName) {
        List<Iris> irisData = new ArrayList<>();
        try {
            // Reading the file line by line
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line by commas to get individual attributes of the iris
                String[] values = line.split(",");
                double petalLength = Double.parseDouble(values[0]);
                double petalWidth = Double.parseDouble(values[1]);
                double sepalLength = Double.parseDouble(values[2]);
                double sepalWidth = Double.parseDouble(values[3]);
                String species = values[4];

                // Create an Iris object and add it to the irisData list
                irisData.add(new Iris(petalLength, petalWidth, sepalLength, sepalWidth, species));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return irisData;  // Return the list of iris objects
    }

    // Method to randomly initialize centroids by selecting random irises from the data
    public static List<Centroid> initializeCentroids(List<Iris> data, int k) {
        Random rand = new Random();
        List<Centroid> centroids = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            // Randomly select an iris and create a centroid from it
            Iris iris = data.get(rand.nextInt(data.size()));
            centroids.add(new Centroid(iris.petalLength, iris.petalWidth, iris.sepalLength, iris.sepalWidth));
        }
        return centroids;  // Return the list of initialized centroids
    }

    // Method to assign each iris to the nearest centroid (cluster assignment step)
    public static List<List<Iris>> assignToClusters(List<Iris> data, List<Centroid> centroids) {
        List<List<Iris>> clusters = new ArrayList<>();
        // Initialize empty clusters for each centroid
        for (int i = 0; i < centroids.size(); i++) {
            clusters.add(new ArrayList<>());
        }

        // Iterate over each iris and assign it to the closest centroid
        for (Iris iris : data) {
            double minDistance = Double.MAX_VALUE;
            int clusterIndex = 0;
            // Loop through each centroid and calculate the distance
            for (int i = 0; i < centroids.size(); i++) {
                double distance = calculateDistance(iris, centroids.get(i));
                // If the distance to the centroid is smaller, update the closest centroid
                if (distance < minDistance) {
                    minDistance = distance;
                    clusterIndex = i;
                }
            }
            // Add the iris to the corresponding cluster
            clusters.get(clusterIndex).add(iris);
        }
        return clusters;  // Return the list of clusters
    }

    // Method to update the centroids based on the average of all points in each cluster
    public static List<Centroid> updateCentroids(List<List<Iris>> clusters) {
        List<Centroid> newCentroids = new ArrayList<>();
        // Calculate the new centroid for each cluster
        for (List<Iris> cluster : clusters) {
            double sumPetalLength = 0, sumPetalWidth = 0, sumSepalLength = 0, sumSepalWidth = 0;
            for (Iris iris : cluster) {
                sumPetalLength += iris.petalLength;
                sumPetalWidth += iris.petalWidth;
                sumSepalLength += iris.sepalLength;
                sumSepalWidth += iris.sepalWidth;
            }
            int size = cluster.size();
            // Calculate the average (new centroid) for each attribute
            newCentroids.add(new Centroid(sumPetalLength / size, sumPetalWidth / size, sumSepalLength / size, sumSepalWidth / size));
        }
        return newCentroids;  // Return the updated list of centroids
    }

    // Method to output the clustering results to a CSV file
    public static void outputResultsToFile(String fileName, List<List<Iris>> clusters) {
        try {
            // Write the clustering results to a file
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            // Write the header row for the CSV file (Cluster, Setosa, Versicolor, Virginica, Total)
            writer.write("Cluster,Setosa,Versicolor,Virginica,Total\n");

            // Initialize an array to store the total counts for each species
            int[] speciesTotals = new int[3];

            // Loop through each cluster and count the number of irises per species
            for (int i = 0; i < clusters.size(); i++) {
                List<Iris> cluster = clusters.get(i);
                Map<String, Integer> speciesCount = new HashMap<>();
                for (Iris iris : cluster) {
                    speciesCount.put(iris.species, speciesCount.getOrDefault(iris.species, 0) + 1);
                }

                // Prepare the row for this cluster and append the species counts
                StringBuilder row = new StringBuilder();
                row.append("Cluster " + (i + 1));  // Label for the cluster

                int totalInCluster = 0;
                for (String species : new String[]{"Iris-setosa", "Iris-versicolor", "Iris-virginica"}) {
                    int speciesCountInCluster = speciesCount.getOrDefault(species, 0);
                    row.append(",").append(speciesCountInCluster);  // Add the count for the species
                    speciesTotals[Arrays.asList("Iris-setosa", "Iris-versicolor", "Iris-virginica").indexOf(species)] += speciesCountInCluster;
                    totalInCluster += speciesCountInCluster;
                }

                row.append(",").append(totalInCluster);  // Add the total count for the cluster
                writer.write(row.toString() + "\n");
            }

            // Write the totals row for all clusters (total count for each species and overall)
            StringBuilder totalRow = new StringBuilder();
            totalRow.append("Total");
            int grandTotal = 0;
            for (int i = 0; i < speciesTotals.length; i++) {
                totalRow.append(",").append(speciesTotals[i]);
                grandTotal += speciesTotals[i];
            }
            totalRow.append(",").append(grandTotal);  // Add the grand total
            writer.write(totalRow.toString() + "\n");

            writer.close();  // Close the file writer after writing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Load the iris dataset from a file
        List<Iris> data = readData("./iris.txt");
        int k = 3;  // Number of clusters 
        // Initialize centroids randomly from the data
        List<Centroid> centroids = initializeCentroids(data, k);

        boolean centroidsChanged;
        List<List<Iris>> clusters = new ArrayList<>();
        // Loop until centroids stop changing
        do {
            clusters = assignToClusters(data, centroids);  // Assign each iris to the closest centroid
            List<Centroid> newCentroids = updateCentroids(clusters);  // Update centroids based on the new cluster assignments

            centroidsChanged = false;
            // Check if centroids have changed, and if so, repeat the process
            for (int i = 0; i < centroids.size(); i++) {
                if (!centroids.get(i).equals(newCentroids.get(i))) {
                    centroidsChanged = true;
                    break;
                }
            }

            centroids = newCentroids;  // Set the new centroids
        } while (centroidsChanged);

        // Write the clustering results to a CSV file
        outputResultsToFile("clustering_results.csv", clusters);
        System.out.println("Clustering results have been written to 'clustering_results.csv'.");
    }
}
