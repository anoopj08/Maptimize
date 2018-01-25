package anoopjain.maptimize;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;

import java.util.ArrayList;

import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.EdgeWeightedGraph;
import edu.princeton.cs.algs4.Graph;

/**
 * Created by anoopjain on 10/25/17.
 */

public class OptimizeRoute {

    private static final String baseUri = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial";

    protected static ArrayList<Place> optimizeRoute(ArrayList<Place> placesList, String currLoc) {
        try {
            EdgeWeightedDigraph mapGraph = createGraph(placesList, currLoc);
        }catch(Exception e){
            e.printStackTrace();
        }
        return placesList;
    }

    private static EdgeWeightedDigraph createGraph(ArrayList<Place> placesList, String currLoc) throws Exception {
        EdgeWeightedDigraph retGraph = new EdgeWeightedDigraph(placesList.size());
        GeoApiContext.Builder contextBuilder = new GeoApiContext.Builder();//setApiKey("AIzaSyAGKPBYxOUYrvRwkTD9aZU_lPdMTPKmGz8").build();
        contextBuilder.apiKey("AIzaSyAGKPBYxOUYrvRwkTD9aZU_lPdMTPKmGz8");
        GeoApiContext context = contextBuilder.build();
        String[] origins = new String[placesList.size()+1];
        String[] destinations = new String[placesList.size()];
        for(int i = 0 ; i < placesList.size() ; i ++){
            origins[i] = placesList.get(i).getAddress().toString();
            destinations[i] = placesList.get(i).getAddress().toString();
        }
        origins[placesList.size()] = currLoc; //TODO: JUST MAKE EVERYTHING A LATLNG
        //destinations[placesList.size()] = currLoc;
        DistanceMatrix matrix = DistanceMatrixApi.getDistanceMatrix(context,origins,destinations).await();//
        for (int i = 0; i < placesList.size(); i++) {
            for (int j = 0; j < placesList.size(); j++) {
                //add edge to graph with weight between two places
                if (i != j) {
                    DirectedEdge de = new DirectedEdge(i, j, matrix.rows[i].elements[j].duration.inSeconds);
                    retGraph.addEdge(de);
                }
            }
        }
        return retGraph;
    }
}
