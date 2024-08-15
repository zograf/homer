import axios from "axios";
import {API} from "../../environment";
import debounce from "lodash/debounce"
import React, { useState } from "react";
import {DropDownInput, DropDownSelect} from "../../components/dropdown/DropDownInput";

export default function AirConditionModule({module, device, token}) {
    const [temperature, setTemperature] = useState(module.currentTemperature)
    const [id, setId] = useState(null)

    const increaseTemperature = () => {
        if ((temperature + 1) <= module.max_TEMP)
            setTemperature(temperature+1)
        onClickTemperature(temperature+1)
    }

    const decreaseTemperature = () => {
        if ((temperature - 1) >= module.min_TEMP)
            setTemperature(temperature-1)
        onClickTemperature(temperature-1)
    }

    const onClickMode = () => {
        let payload = new FormData()
        payload.append("deviceId", device.id)
        payload.append("temperature", temperature)
        payload.append("mode", mode)
        console.log(mode)
        console.log(token)

        axios.post(API + "/device/airConditioner/mode", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
            })
    }

    const onClickTemperature = (temp) => {
        let payload = new FormData()
        payload.append("deviceId", device.id)
        payload.append("temperature", temp)
        console.log(temperature)

        axios.post(API + "/device/airConditioner/temperature", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
            })
    }

    const increaseClick = () => {
        increaseTemperature();
    }

    const decreaseClick = () => {
        decreaseTemperature();
    }

    const [modes, setModes] = useState([
        { label: "AUTOMATIC", value: "AUTOMATIC" },
        { label: "HEATING", value: "HEATING" },
        { label: "COOLING", value: "COOLING" },
        { label: "VENTILATION", value: "VENTILATION" },
    ])
    const [mode, setMode] = useState(module.currentMode)

    const modeCallback = (item) => {
        if(item !== undefined){
            setMode(item)
        }else{
            setMode(module.currentMode)
        }
    }

    return(
        <div className="flex columns space-between">
            <p className="card-title neutral v-spacer-s">Set values</p>
            <div className="flex gap-l center">
                <div>
                    <button className="text-button small-button" style={{padding:"4px"}}>
                        <span className="material-symbols-outlined icon" onClick={increaseClick}>add</span>
                    </button>

                    <p className="section-title v-spacer-s">{temperature} °C</p>

                    <button className="text-button small-button" style={{padding:"4px"}}>
                        <span className="material-symbols-outlined icon" onClick={decreaseClick}>remove</span>
                    </button>
                </div>

                <div>
                    <DropDownSelect
                        placeholder={"Mode"} 
                        options={modes} 
                        callback={modeCallback}/>

                    <button className="small-button w-100 solid-button" style={{marginTop: "8px"}} onClick={onClickMode}>Set mode</button>
                </div>
            </div>
        </div>
    )
}
