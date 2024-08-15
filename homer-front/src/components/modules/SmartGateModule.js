import axios from "axios";
import {API} from "../../environment";

export default function SmartGateModule({module, device, token}) {

    const onOpenCloseClick = (event) => {
        event.stopPropagation();
        module.open = !module.open

        let payload = new FormData()
        payload.append("deviceId", device.id)
        payload.append("isOpen", module.open)

        axios.post(API + "/device/gate/isOpen", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
                // TODO On auto change fail
            })
    }
    const onPrivatePublicClick = (event) => {
        event.stopPropagation();
        module.private = !module.private

        let payload = new FormData()
        payload.append("deviceId", device.id)
        payload.append("isPrivate", module.private)
        console.log(module)

        axios.post(API + "/device/gate/isPrivate", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
                // TODO On auto change fail
            })
    }

    return(
        <div className="flex columns space-between" style={{minWidth:"148px"}}>
            <p className="card-title neutral">Controls</p>
            <div>
                <button className={`small-button w-100 v-spacer-xs ${module.open ? 'outline-button' : 'solid-button'}`} onClick={onOpenCloseClick}>{module.open ? "Opened" : "Closed"}</button>
                <button className={`small-button w-100 ${module.private ? 'solid-button' : 'outline-button'}`} onClick={onPrivatePublicClick}>{module.private ? "Private Mode" : "Public Mode"}</button>
            </div>
        </div>
    )
}