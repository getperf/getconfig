package com.getconfig.Document

import com.google.common.collect.*
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomUniformGeneratedInitialMeans
import de.lmu.ifi.dbs.elki.data.Cluster
import de.lmu.ifi.dbs.elki.data.Clustering
import de.lmu.ifi.dbs.elki.data.NumberVector
import de.lmu.ifi.dbs.elki.data.model.KMeansModel
import de.lmu.ifi.dbs.elki.data.type.TypeUtil
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter
import de.lmu.ifi.dbs.elki.database.ids.DBIDRange
import de.lmu.ifi.dbs.elki.database.relation.Relation
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction
import de.lmu.ifi.dbs.elki.logging.LoggingConfiguration
import de.lmu.ifi.dbs.elki.utilities.random.RandomFactory
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.ToString
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
@CompileStatic
@ToString(includePackage = false)
class KMeansCluster {
    static Multimap<Integer, Integer> run(
            double[][] data, int partitions) {
        LoggingConfiguration.setStatistics();

        def dbc = new ArrayAdapterDatabaseConnection(data);
        def db = new StaticArrayDatabase(dbc, null);
        db.initialize();
        Relation<NumberVector> rel = db.getRelation(
                TypeUtil.NUMBER_VECTOR_FIELD);
        DBIDRange ids = (DBIDRange) rel.getDBIDs();

        // K-means should be used with squared Euclidean (least squares):
        def dist = SquaredEuclideanDistanceFunction.STATIC;

        // Default initialization, using global random:
        // To fix the random seed, use: new RandomFactory(seed);
        // RandomFactory rnd = new RandomFactory(1);
        def init = new RandomUniformGeneratedInitialMeans(RandomFactory.DEFAULT);

        // Textbook k-means clustering:
        KMeansLloyd<NumberVector> km = new KMeansLloyd<>(dist, //
                partitions /* k - number of partitions */, //
                0 /* maximum number of iterations: no limit */, init);

        // K-means will automatically choose a numerical relation from the data set:
        // But we could make it explicit (if there were more than one numeric
        // relation!): km.run(db, rel);
        Clustering<KMeansModel> c = km.run(db);

        // Output all clusters:
        Multimap<Integer, Integer> clusters = HashMultimap.create()
        int i = 0;
        for(Cluster<KMeansModel> clu : c.getAllClusters()) {
            // K-means will name all clusters "Cluster" in lack of noise support:
            log.info "#" + i + ": " + clu.getNameAutomatic()
            log.info "Size: " + clu.size()

            for(DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
                // To get the vector use:
                // NumberVector v = rel.get(it);
                // Offset within our DBID range: "line number"
                final int offset = ids.getOffset(it);
                // System.out.print(" " + offset);
                clusters.put(i, offset)
                // Do NOT rely on using "internalGetIndex()" directly!
            }
            log.info "#${i}:${clu.getNameAutomatic()}, ${clusters.get(i)}"
            ++i;
        }
        return clusters
    }
}
