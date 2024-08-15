import React, {useState} from "react";
import {StandardPopUp, usePopup} from "../popup/PopUpFrame";
import {DropDownInput, DropDownSelect} from "../../components/dropdown/DropDownInput";
import axios from "axios";
import {API} from "../../environment";

export default function WashingMachineSchedulingModule({deviceId, acModule, schedulingModule}) {
    const token = localStorage.getItem("token")

    const [start, setStart] = useState(null)
    const [isRepeat, setIsRepeat] = useState(false)
    const handleStart = (e) => setStart(e.target.value);
    const handleIsRepeat = (e) => setIsRepeat(e.target.checked);

    const addSchedule = () => {
        let payload = new FormData()
        payload.append("deviceId", deviceId)
        payload.append("start", start)
        payload.append("mode", mode)
        payload.append("isRepeat", isRepeat)

        axios.post(API + "/device/washingMachine/schedule/add", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
            })
        setStart(null)
    }

    const deletePopup = usePopup()

    const deleteSchedule = (id) => {
        let payload = new FormData()
        payload.append("deviceId", deviceId)
        payload.append("scheduleId", id)

        console.log(id)

        axios.post(API + "/device/washingMachine/schedule/remove", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
            })
    }

    const [modes, setModes] = useState([
        { label: "40 degree wash", value: "WASH_40" },
        { label: "90 degree wash", value: "WASH_90" },
        { label: "Rinse", value: "RINSE" },
        { label: "Spin", value: "SPIN" },
    ])
    const [mode, setMode] = useState(module.currentMode)

    const modeCallback = (item) => {
        if(item !== undefined){
            console.log(item)
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
                    <p>Start:</p>
                    <input placeholder="Start time" value={start} onChange={handleStart} type="datetime-local"/>
                </div>
            </div>
            <div className="flex gap-s center v-spacer-xs">
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
            <div className="flex gap-s center v-spacer-xs">
            </div>
            <div className="gap-xs" style={{display:"grid", gridTemplateRows:"repeat(auto-fill, 100px)", maxWidth: "300px"}}>
                {
                    schedulingModule.scheduleList.map((item) => (
                    <div className="card flex space-between center">
                        <div className="data-grid" style={{ margin: '4px 0' }}>
                            <p className="key-item">Start: </p> <p className="value-item">{d(item.startTime)}</p>
                            <p className="key-item">End: </p> <p className="value-item">{d(item.endTime)}</p>
                            <p className="key-item">Mode: </p>
                            <p className="value-item">{m(item.command.split("|")[1])}</p>
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
