package com.example.findmyfriends.data.model

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

data class DataUser (
    var lat: Double,
    var lng: Double,
    private var title: String,
    private var snippet: String,
) : ClusterItem {
    private val position: LatLng = LatLng(lat, lng)

    override fun getPosition(): LatLng {
        return position
    }

    override fun getTitle(): String {
        return title
    }

    override fun getSnippet(): String {
        return snippet
    }

    fun getZIndex(): Float {
        return 0f
    }

    init {
        this.title = title
        this.snippet = snippet
    }
}

class UserRenderer(
    private var context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<DataUser>
): DefaultClusterRenderer<DataUser>(context, map, clusterManager){
    override fun shouldRenderAsCluster(cluster: Cluster<DataUser>): Boolean {
        //return super.shouldRenderAsCluster(cluster)
        return cluster.size > 1
    }

    override fun onBeforeClusterItemRendered(item: DataUser, markerOptions: MarkerOptions) {
//        super.onBeforeClusterItemRendered(item, markerOptions)
        var marker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        markerOptions.title(item.title).position(item.position).snippet(item.snippet)//.icon(marker)
    }

    override fun onClusterItemRendered(clusterItem: DataUser, marker: Marker) {
//        super.onClusterItemRendered(clusterItem, marker)
        marker.tag = clusterItem
        marker.showInfoWindow()

    }
}