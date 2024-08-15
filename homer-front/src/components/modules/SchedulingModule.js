import React, {useState} from "react";
import {StandardPopUp, usePopup} from "../popup/PopUpFrame";
import {DropDownInput, DropDownSelect} from "../../components/dropdown/DropDownInput";
import axios from "axios";
import {API} from "../../environment";

export default function SchedulingModule({deviceId, acModule, schedulingModule}) {
    const token = localStorage.getItem("token")

    const [start, setStart] = useState(null)
    const [end, setEnd] = useState(null)
    const [isRepeat, setIsRepeat] = useState(false)
    const handleStart = (e) => setStart(e.target.value);
    const handleEnd = (e) => setEnd(e.target.value);
    const handleIsRepeat = (e) => setIsRepeat(e.target.checked);

    const [temperature, setTemperature] = useState(20)

    const increaseTemperature = () => {
        if ((temperature + 1) <= acModule.max_TEMP)
            setTemperature(temperature+1)
    }

    const decreaseTemperature = () => {
        if ((temperature - 1) >= acModule.min_TEMP)
            setTemperature(temperature-1)
    }

    const addSchedule = () => {
        let payload = new FormData()
        payload.append("deviceId", deviceId)
        payload.append("start", start)
        payload.append("end", end)
        payload.append("mode", mode)
        payload.append("temperature", temperature)
        payload.append("isRepeat", isRepeat)
        console.log(isRepeat)

        axios.post(API + "/device/airConditioner/schedule/add", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
            })
        setStart(null)
        setEnd(null)
        setTemperature(20)
    }

    const deletePopup = usePopup()

    const deleteSchedule = (id) => {
        let payload = new FormData()
        payload.append("deviceId", deviceId)
        payload.append("scheduleId", id)

        console.log(id)

        axios.post(API + "/device/airConditioner/schedule/remove", payload, { headers: {"Authorization" : `Bearer ${token}`} })
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

    const d = (val) => {
        console.log(val)
        if (val.length > 1) {
            return val[2] + "/" + val[1] + "/" + val[0] + " at " + val[3] + ":" + val[4]
        }
    }
    
    const m = (mode) => {
        return modes.filter(item => item.value === mode)[0].label
    }

    return(
        <div>
            <p className="section-title">Add schedule</p>
            <div className="flex gap-s center v-spacer-xs">
                <div className="input-wrapper regular-border v-spacer-xs">
                    <p className="input-icon">From</p>
                    <input placeholder="Start time" value={start} onChange={handleStart} type="datetime-local"/>
                </div>
                <div className="input-wrapper regular-border v-spacer-xs">
                    <p className="input-icon">To</p>
                    <input placeholder="End time" value={end} onChange={handleEnd} type="datetime-local"/>
                </div>
            </div>
            <div className="flex gap-s center v-spacer-xs">
                <button className="text-button small-button" style={{padding:"4px"}}>
                    <span className="material-symbols-outlined icon" onClick={decreaseClick}>remove</span>
                </button>
                <p className="section-title v-spacer-s">{temperature}</p>
                <button className="text-button small-button" style={{padding:"4px"}}>
                    <span className="material-symbols-outlined icon" onClick={increaseClick}>add</span>
                </button>
                <DropDownSelect
                    placeholder={"Mode"} 
                    options={modes} 
                    callback={modeCallback}/>
                <p className="section-title">Daily:</p>
                <div className="regular-border v-spacer-xs">
                    <input value={isRepeat} onChange={handleIsRepeat} type="checkbox"/>
                </div>
                <button className="solid-button" onClick={addSchedule}>Add schedule</button>
            </div>
            <div className="gap-xs" style={{display:"grid", gridTemplateRows:"repeat(auto-fill, 150px)", maxWidth: "300px"}}>
                {
                    schedulingModule.scheduleList.map((item) => (
                    <div className="card flex space-between center">
                        <div className="data-grid" style={{ margin: '4px 0' }}>
                            <p className="key-item">Start: </p> <p className="value-item">{d(item.startTime)}</p>
                            <p className="key-item">End: </p> <p className="value-item">{d(item.endTime)}</p>
                            <p className="key-item">Mode: </p>
                            <p className="value-item">{m(item.command.split("|")[1])}</p>
                            <p className="key-item">Temperature: </p>
                            <p className="value-item">{item.command.split("|")[3]}°C</p>
                            <p className="key-item">Daily: </p>
                            <p className="value-item">{item.repeat ? "Yes" : "No"}</p>
                        </div>
                        <button className="text-button small-button" style={{padding:"4px"}}>
                            <span className="material-symbols-outlined icon" onClick={() => { deletePopup.showPopup(item)} }>cancel</span>
                        </button>
                    </div>
                ))
                }
            </div>
            <DeletePopUp popup={deletePopup} deleteCallback={deleteSchedule}/>
        </div>
    )
}

function DeletePopUp({popup, deleteCallback}) {

    const [msg, setMsg] = useState("")
    const handleDeletePopUp = (id) => {
        deleteCallback(id.id, msg)
        setMsg("")
    }

    return(
        <StandardPopUp
            visible={popup.isVisible}
            title={"Confirm Schedule Delete"}
            description={"Are you sure you want to delete this scheduled item?"}
            neutralLabel={"Cancel"}
            positiveLabel={"Delete"}
            positiveCallback={popup.onPositiveCallback(handleDeletePopUp)}
            neutralCallback={popup.onNeutralCallback()}/>
    )
}
