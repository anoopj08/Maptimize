package anoopjain.maptimize;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;

import java.util.ArrayList;

import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.EdgeWeightedGraph;
import edu.princeton.cs.algs4.Graph;

/**
 * Created by anoopjain on 10/25/17.
*/

public class OptimizeRoute{

    private static final String baseUri = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial";

    protected static ArrayList<Place> optimizeRoute(ArrayList<Place> placesList){

        EdgeWeightedDigraph mapGraph = createGraph(placesList);
        return placesList;
    }

    private static EdgeWeightedDigraph createGraph(ArrayList<Place> placesList){
        EdgeWeightedDigraph retGraph = new EdgeWeightedDigraph(placesList.size());
        GeoApiContext context = new GeoApiContext();//.setApiKey("AIzaSyAGKPBYxOUYrvRwkTD9aZU_lPdMTPKmGz8");

        for(int i = 0 ; i < placesList.size(); i++){
            for(int j = 0 ; j < placesList.size(); j++){
                //add edge to graph with weight between two places
                if(i != j) {
                    StringBuilder uri = new StringBuilder(baseUri);
                    uri.append("&origins=" + placesList.get(i).getAddress());
                    uri.append("&destinations=" + placesList.get(j).getAddress());
                    Log.i("DISTANCEMATRIX: ", uri.toString());
                    DistanceMatrixApiRequest req = DistanceMatrixApi.getDistanceMatrix();
                    //dispToast.makeText(, uri.toString(), Toast.LENGTH_LONG).show();

                    //DirectedEdge de = new DirectedEdge();
                }
            }
        }
        return null;
    }
}
