import axios from "axios";
import {API} from "../../environment";
import { useState } from "react";

export default function ShareModule({device, token}) {
    const [msg, setMsg] = useState("")

    const handleShare = () => {
        // PropertyId, Email
        let payload = { "propertyId": null, "userEmail": msg, "ownerEmail": localStorage.getItem("username"), "deviceId": device.id }
        axios.post(API + `/user/${localStorage.getItem("id")}/devicePermission`, payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log("PERMISSION GRANTED")
            })
            .catch(e => console.log(e))
    }

    return(
        <div className="flex columns space-between">
            <p className="card-title neutral">Share</p>
            <div className="input-wrapper regular-border">
                <span className="material-symbols-outlined icon input-icon">email</span>
                <input placeholder="Email" value={msg} onChange={(e) => setMsg(e.target.value)}/>
            </div>
            <button className={`small-button w-100 solid-button`} onClick={handleShare}>SHARE</button>
        </div>
    )
}