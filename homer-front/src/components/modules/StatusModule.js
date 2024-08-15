import axios from "axios";
import {API} from "../../environment";

export default function StatusModule({module, device, token}) {

    const onClick = (event) => {
        event.stopPropagation();
        
        let payload = new FormData()
        payload.append("deviceId", device.id)
        payload.append("on", !module.on)

        axios.post(API + "/device/on", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
                // TODO On status change failed
            })
    }

    return(
        <div className="flex columns space-between">
            <p className="card-title neutral">Status</p>
            <button className={`small-button w-100 ${module.on ? 'solid-button' : 'outline-button'}`} onClick={onClick}>{module.on ? "On" : "Off"}</button>
        </div>
    )
}