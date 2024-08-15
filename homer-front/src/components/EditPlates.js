import React, {useState} from "react";
import {StandardPopUp, usePopup} from "./popup/PopUpFrame";
import axios from "axios";
import {API} from "../environment";
import {NoDevice} from "../pages/devices/components/DeviceCard";

export default function EditPlates({deviceId, plates = []}) {
    const token = localStorage.getItem("token")

    const [value, setValue] = useState("")
    const handleValue = (e) => setValue(e.target.value);
    const addPlate = (event) => {
        let payload = new FormData()
        payload.append("deviceId", deviceId)
        payload.append("text", value)

        axios.post(API + "/device/gate/plate/add", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
                // TODO On auto change fail
            })
        setValue("")
    }

    const deletePopup = usePopup()
    const deletePlate = (plate) => {
        let payload = new FormData()
        payload.append("deviceId", deviceId)
        payload.append("plateId", plate.id)

        axios.post(API + "/device/gate/plate/remove", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
                // TODO On auto change fail
            })
        setValue("")
    }

    return(
        <div>
            <p className="section-title">Plates for Private Use</p>
            <div className="flex gap-s center v-spacer-xs">
                <div className="input-wrapper regular-border v-spacer-xs">
                    <span className="material-symbols-outlined icon input-icon">car_rental</span>
                    <input placeholder="Plate Number (Full)" value={value} onChange={handleValue}/>
                    <button className="text-button small-button" onClick={addPlate}>Add</button>
                </div>
            </div>
            <div className="v-spacer-l">
                <div className="gap-xs" style={{display:"grid", gridTemplateColumns:"repeat(auto-fill, 164px)"}}>
                    {plates.map((plate) => (
                        <div className="card flex space-between center">
                            <p className="card-title">{plate.text}</p>
                            <button className="text-button small-button" style={{padding:"4px"}}>
                                <span className="material-symbols-outlined icon" onClick={() => { deletePopup.showPopup(plate)} }>cancel</span>
                            </button>
                        </div>
                    ))}
                </div>
                {plates.length === 0 &&
                    <div className="dashed-container flex center justify-center columns showing">
                        <p className="card-title">You don't have any plates.</p>
                        <p className="card-label">Add Plates so you the gate will open for you even in private mode.</p>
                    </div>
                }
            </div>
            <DeletePopUp popup={deletePopup} deleteCallback={deletePlate}/>
        </div>
    )
}

function DeletePopUp({popup, deleteCallback}) {

    const [msg, setMsg] = useState("")
    const handleDeletePopUp = (id) => {
        deleteCallback(id, msg)
        setMsg("")
    }

    return(
        <StandardPopUp
            visible={popup.isVisible}
            title={"Confirm Plate Delete"}
            description={"Are you sure you want to delete this plate?"}
            neutralLabel={"Cancel"}
            positiveLabel={"Delete"}
            positiveCallback={popup.onPositiveCallback(handleDeletePopUp)}
            neutralCallback={popup.onNeutralCallback()}/>
    )
}