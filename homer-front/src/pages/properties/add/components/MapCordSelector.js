import {MapContainer, Marker, TileLayer, useMapEvent} from "react-leaflet";
import React, {useState} from "react";
import {Icon} from "leaflet";

export function MapCordSelector({center = [45.239113481324445, 19.83919235521642], zoom = 13, callback}) {
    return(
        <div className="card map-wrapper regular-border">
            <MapContainer style={{zIndex: "1"}} center={center} zoom={zoom}>
                <TileLayer url="https://tiles.stadiamaps.com/tiles/alidade_smooth/{z}/{x}/{y}{r}.png" />
                <HomeMarker callback={callback}/>
            </MapContainer>
            <p className="card-body map-label neutral">Map Coordinates are required</p>
        </div>
    )
}

function HomeMarker({callback}) {
    const [position, setPosition] = useState(null)
    const pin = new Icon({
        iconUrl: "https://static.thenounproject.com/png/510235-200.png",
        iconSize: [38, 38]
    })
    useMapEvent({
        click(event) {
            setPosition([event.latlng.lat, event.latlng.lng])
            callback([event.latlng.lat, event.latlng.lng])
        }
    })
    return position === null ? null : (
        <Marker position={position} icon={pin}/>
    )
}
