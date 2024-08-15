import React, {useEffect, useRef, useState} from "react";
import "./AddPropertyPage.css"

import { MapContainer, TileLayer, Marker, useMapEvent } from "react-leaflet";
import { Icon } from "leaflet";
import { UserPage } from '../../root/UserPage'
import { CitySelector } from "./components/CitySelector";
import {MapCordSelector} from "./components/MapCordSelector";
import axios from "axios";
import {API} from "../../../environment";


export default function AddPropertyPage() {
    const token = localStorage.getItem("token")

    const [isValid, setIsValid] = useState(false)
    const [name, setName] = useState("")
    const [floors, setFloors] = useState("")
    const [area, setArea] = useState("")
    const [displayAddress, setDisplayAddress] = useState("")
    const [countryId, setCountryId] = useState(undefined)
    const [cityId, setCityId] = useState(undefined)
    const [latLon, setLatLon] = useState(undefined)

    const [image, setImage] = useState(undefined)
    const imageRef = useRef(null)

    useEffect(() => {
        setIsValid(
            name.length > 0
            && floors !== ""
            && area !== ""
            && displayAddress.length > 0
            && countryId !== undefined
            && cityId !== undefined
            && latLon !== undefined
            && image !== undefined
        )
    }, [name, floors, area, displayAddress, countryId, cityId, latLon, image])

    const citySelected = (cityLocation) => {
        setCountryId(cityLocation.countryId)
        setCityId(cityLocation.cityId)
    }
    const positionChanged = (latLon) => setLatLon(latLon)

    const submit = () => {

        let payload = new FormData()
        payload.append("name", name)
        payload.append("floors", floors)
        payload.append("area", area)
        payload.append("displayAddress", displayAddress)
        payload.append("countryId", countryId)
        payload.append("cityId", cityId)
        payload.append("lat", latLon[0])
        payload.append("lon", latLon[1])
        payload.append("image", image)

        axios.post(API + "/property", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
                //alert("Successfully sent a Request!")
                console.log("Successfully sent a Request!")
                window.location.href = "/devices"
            })
            .catch(e =>
                //alert("Failed")
                console.log("Failed")
            )
    }

    return(
        <UserPage>
            <div className="main">
                <h1 className="page-title">Add Property</h1>
                <div className="wrapper gap-s">
                    <div>
                        <div className="card flex justify-center v-spacer-s columns">
                            <img
                                alt="not found"
                                onClick={() => imageRef.current.click()}
                                className="select-image regular-border v-spacer-xs"
                                src={image == null ? 'https://www.ilovegemhomes.com/app/themes/carrot/assets/images/property-placeholder.png' : URL.createObjectURL(image)}
                            />
                            <input
                                style={{display: "none"}}
                                type="file"
                                name="image"
                                onChange={(e) => setImage(e.target.files[0])}
                                ref={imageRef}>
                            </input>
                            <p className="card-body h-padding-xs neutral">Image is required</p>
                        </div>
                        <div className="card v-spacer-s">
                            <div className="input-wrapper regular-border v-spacer-xs">
                                <span className="material-symbols-outlined icon input-icon">home_work</span>
                                <input placeholder="Name*" type={"text"} value={name} onChange={(e) => setName(e.target.value)}/>
                            </div>
                            <div className="two-input-wrapper v-spacer-xs">
                                <div className="input-wrapper regular-border">
                                    <span className="material-symbols-outlined icon input-icon">layers</span>
                                    <input placeholder="Floors*" type={"number"} value={floors} onChange={(e) => setFloors(parseInt(e.target.value))}/>
                                </div>
                                <div className="input-wrapper regular-border">
                                    <span className="material-symbols-outlined icon input-icon">crop</span>
                                    <input placeholder="Total Area [m2]*" type={"number"} value={area} onChange={(e) => setArea(parseInt(e.target.value))}/>
                                </div>
                            </div>
                            <CitySelector token={token} callback={citySelected}/>
                            <div className="input-wrapper regular-border v-spacer-xs">
                                <span className="material-symbols-outlined icon input-icon">signpost</span>
                                <input placeholder="Display Street Name*" type={"text"} value={displayAddress} onChange={(e) => setDisplayAddress(e.target.value)}/>
                            </div>
                        </div>
                        <div className="flex justify-end"><button className={['solid-accent-button', isValid ? 'solid-accent-button' : 'disabled-solid-button'].join(" ")} disabled={!isValid} onClick={submit}>Send Creation Request</button></div>

                    </div>
                    <MapCordSelector callback={positionChanged}/>
                </div>
            </div>
        </UserPage>
    )
}


