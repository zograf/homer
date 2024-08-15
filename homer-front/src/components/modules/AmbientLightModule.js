import axios from "axios";
import {API} from "../../environment";

export default function AmbientLightModule({module, device, token}) {

    const onClick = (event) => {
        event.stopPropagation();
        module.autoStatus = !module.autoStatus

        let payload = new FormData()
        payload.append("deviceId", device.id)
        payload.append("autoStatus", module.autoStatus)

        axios.post(API + "/device/ambientLight/auto", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
                // TODO On auto change fail
            })
    }

    return(
        <div className="flex columns space-between">
            <p className="card-title neutral">Ambient Light</p>
            <div className="data-grid" style={{ margin: '4px 0' }}>
                <p className="key-item">Amount:</p>
                <p className="value-item">{module.lightPresence} %</p>
                <p className="key-item">Auto Status:</p>
                <p className="value-item">{module.autoStatus ? "Enabled" : "Disabled"}</p>
            </div>
            <button className="small-button w-100 solid-button" onClick={onClick}>{module.autoStatus ? "Disable Auto" : "Enable Auto"}</button>
        </div>
    )
}