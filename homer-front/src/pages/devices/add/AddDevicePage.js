import React, {useEffect, useRef, useState} from "react";
import "./AddDevicePage.css"

import { UserPage } from '../../root/UserPage'
import axios from "axios";
import {API} from "../../../environment";
import {DropDownInput, DropDownSelect} from "../../../components/dropdown/DropDownInput";
import CircleIconButton from "../../../components/buttons/CircleIconButton";


export default function AddDevicePage() {
    const token = localStorage.getItem("token")

    const [isValid, setIsValid] = useState(false)
    const [name, setName] = useState("")
    const [propertyId, setPropertyId] = useState(undefined)
    const [type, setType] = useState("")
    const [powerSupply, setPowerSupply] = useState("")
    const [consumption, setConsumption] = useState(undefined)

    // Solar panel system
    const [numPanels, setNumPanels] = useState(undefined)
    const [area, setArea] = useState(undefined)
    const [efficiency, setEfficiency] = useState(undefined)

    // Battery
    const [capacity, setCapacity] = useState(undefined)

    // Plates
    const [plateValue, setPlateValue] = useState("")
    const [plates, setPlates] = useState([])

    const [image, setImage] = useState(undefined)
    const imageRef = useRef(null)

    const [types, setTypes] = useState([
        {label: "Ambient sensor", value: "AMBIENT_SENSOR"},
        {label: "Air conditioner", value: "AIR_CONDITIONER"},
        {label: "Washing machine", value: "WASHING_MACHINE"},
        {label: "Lamp", value: "LAMP"},
        {label: "Vehicle gate", value: "GATE"},
        {label: "Sprinkler system", value: "SPRINKLER_SYSTEM"},
        {label: "Solar panel system", value: "SOLAR_PANEL_SYSTEM"},
        {label: "Battery", value: "BATTERY"},
        {label: "Electric vehicle charger", value: "EV_CHARGER"}
    ])

    const [powerSupplyTypes, setSupplyTypes] = useState([
        {label: "Autonomous power supply", value: "AUTONOMOUS"},
        {label: "Home power supply", value: "HOME"}
    ])

    const [properties, setProperties] = useState([])

    const getProperties = () => {
        axios.get(API + "/property/accepted", { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => { setProperties(response.data.map((item) => { return { label: item.name, value: item }})) })
            .catch(e => console.log(e))
    }
    useEffect(() => getProperties(), [])

    useEffect(() => {
        if (type === "SOLAR_PANEL_SYSTEM"){
            setIsValid(
                name !== ""
                && propertyId !== undefined
                && type !== ""
                && image !== undefined
                && numPanels !== undefined
                && area !== undefined
                && efficiency !== undefined
            )
        }else if(type === "BATTERY"){
            setIsValid(
                name !== ""
                && propertyId !== undefined
                && type !== ""
                && image !== undefined
                && capacity !== undefined
            )
        }else{
            setIsValid(
                name !== ""
                && propertyId !== undefined
                && type !== ""
                && powerSupply !== ""
                && consumption !== undefined
                && image !== undefined
            )
        }
    }, [name, propertyId, type, powerSupply, consumption, image, numPanels, area, efficiency, capacity])

    const submit = () => {

        let payload = new FormData()
        payload.append("name", name)
        payload.append("propertyId", propertyId)
        payload.append("type", type)
        payload.append("image", image)

        if (type === "SOLAR_PANEL_SYSTEM") {
            payload.append("numPanels", numPanels)
            payload.append("area", area)
            payload.append("efficiency", efficiency)
            payload.append("powerSupply", "AUTONOMOUS")
            payload.append("consumption", 0.0)
            
        } else if (type === "BATTERY") {
            payload.append("capacity", capacity)
            payload.append("powerSupply", "AUTONOMOUS")
            payload.append("consumption", 0.0)
        } else if (type === "GATE") {
            payload.append("plates", plates.join(','))
            payload.append("powerSupply", powerSupply)
            payload.append("consumption", consumption)
        }
        else {
            payload.append("powerSupply", powerSupply)
            payload.append("consumption", consumption)        
        }

        axios.post(API + "/device", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
                window.location.href = "/devices"
            })
            .catch(e =>
                //alert("Failed")
                console.log("Failed")
            )
    }

    const typeCallback = (item) => {
        if(item !== undefined){
            setType(item)
        }else{
            setType("")
        }
    }

    const powerSupplyTypeCallback = (item) => {
        if(item !== undefined){
            setPowerSupply(item)
        }else{
            setPowerSupply("")
        }
    }

    const propertyCallback = (item) => {
        if(item !== undefined){
            setPropertyId(item.id)
        }else{
            setPropertyId(undefined)
        }
    }

    return(
        <UserPage>
            <div className="main">
                <h1 className="page-title">Add Device</h1>
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
                            <div className="two-input-wrapper">
                                <div className="input-wrapper regular-border v-spacer-xs">
                                    <span className="material-symbols-outlined icon input-icon">power</span>
                                    <input placeholder="Name*" type={"text"} value={name} onChange={(e) => setName(e.target.value)}/>
                                </div>
                                <DropDownSelect placeholder={"Property*"} icon={"home"} options={properties} callback={propertyCallback}/>
                            </div>    

                            <div className={(type !== "SOLAR_PANEL_SYSTEM" && type !== "BATTERY") ? "two-input-wrapper v-spacer-xs" : ""}>
                                <DropDownSelect placeholder={"Device type*"} icon={"power"} options={types} callback={typeCallback}/>
                                { (type !== "SOLAR_PANEL_SYSTEM" && type !== "BATTERY") && <DropDownSelect placeholder={"Power supply type*"} icon={"power"} options={powerSupplyTypes} callback={powerSupplyTypeCallback}/>}
                            </div>

                            { (type !== "SOLAR_PANEL_SYSTEM" && type !== "BATTERY") &&<div className="input-wrapper regular-border v-spacer-xs">
                                <span className="material-symbols-outlined icon input-icon">power</span>
                                <input placeholder="Power consumption [kW]*" type={"number"} step={"0.1"} value={consumption} onChange={(e) => setConsumption(e.target.value)}/>
                            </div> }
                        </div>

                        { type === "SOLAR_PANEL_SYSTEM" && <div>
                            <div className="card v-spacer-s">
                                    
                                <div className="input-wrapper regular-border v-spacer-xs">
                                    <span className="material-symbols-outlined icon input-icon">solar_power</span>
                                    <input placeholder="Number of panels*" type={"number"} value={numPanels} onChange={(e) => setNumPanels(e.target.value)}/>
                                </div>
                                <div className="two-input-wrapper">
                                    <div className="input-wrapper regular-border v-spacer-xs">
                                        <span className="material-symbols-outlined icon input-icon">solar_power</span>
                                        <input placeholder="Area [m2]*" type={"number"} step={"0.1"} value={area} onChange={(e) => setArea(e.target.value)}/>
                                    </div>
                                    <div className="input-wrapper regular-border v-spacer-xs">
                                        <span className="material-symbols-outlined icon input-icon">solar_power</span>
                                        <input placeholder="Efficency [%]*" type={"number"} step={"0.1"} value={efficiency} onChange={(e) => setEfficiency(e.target.value)}/>
                                    </div>
                                </div>

                            </div>
                        </div> }

                        { type === "BATTERY" && <div>
                            <div className="card v-spacer-s">
                                <div className="input-wrapper regular-border v-spacer-xs">
                                    <span className="material-symbols-outlined icon input-icon">solar_power</span>
                                    <input placeholder="Capacity [kWh]*" type={"number"} step={"0.1"} value={capacity} onChange={(e) => setCapacity(e.target.value)}/>
                                </div>
                            </div>
                        </div> }

                        { type === "GATE" && <div>
                            <div className="card v-spacer-s">
                                <div className="input-wrapper regular-border v-spacer-xs">
                                    <span className="material-symbols-outlined icon input-icon">solar_power</span>
                                    <input placeholder="Plates allowed in Private Mode" type={"text"} value={plateValue} onChange={(e) => setPlateValue(e.target.value)}/>
                                    <button className="small-icon-button solid-icon-button" style={{translate:"6px 0"}} onClick={(e) => {
                                        plates.push(plateValue)
                                        setPlateValue("")
                                    }}><span className="material-symbols-outlined icon">add</span></button>
                                </div>
                                <div className="flex gap-xs wrap">
                                    { plates.map((plate) => <div className="input-wrapper regular-border flex gap-xs">
                                        <p className="card-title">{plate}</p>
                                        <button className="small-icon-button outline-icon-button" style={{translate:"6px 0"}} onClick={(e) => {
                                            setPlates((prev) => {
                                                let newPlates = []
                                                prev.forEach((p) => {
                                                    if(p !== plate) newPlates.push(p)
                                                })
                                                return newPlates
                                            })
                                        }}><span className="material-symbols-outlined icon">remove</span></button>
                                    </div> )}
                                </div>
                            </div>
                        </div> }

                        <div className="flex justify-end"><button className={['solid-accent-button', isValid ? 'solid-accent-button' : 'disabled-solid-button'].join(" ")} disabled={!isValid} onClick={submit}>Create</button></div>

                    </div>

                </div>
            </div>
        </UserPage>
    )
}


