import { useState } from "react";
import axios from "axios";
import { API } from "../../environment";

export default function EvChargerModule({module, device, token}) {
    
    const [fillTo, setFillTo] = useState("")

    const onFillToChange = (event) => {
        event.stopPropagation();
        if(event.target.value >= 0 && event.target.value <= 100){
            setFillTo(event.target.value);
        }
    }

    const onClickFillTo = (event) => {
        event.stopPropagation();

        if(fillTo === "")return;
        
        let payload = new FormData()
        payload.append("deviceId", device.id)
        payload.append("percent", fillTo)

        axios.post(API + "/device/percent", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e =>
                //alert("Failed")
                console.log("Failed")
            )
    }

    return(
        <div className="flex columns space-between">
            <p className="card-title neutral">About</p>
            <div className="flex gap-l center">
                <div className="data-grid">
                    <p className="key-item">Power:</p>
                    <p className="value-item">{module.power} kWh</p>
                    <p className="key-item">Slots:</p>
                    <p className="value-item">{module.slots}</p>
                    <p className="key-item">Occupied slots:</p>
                    <p className="value-item">{module.occupiedSlots}</p>
                    <p className="key-item">Fill to:</p>
                    <p className="value-item">{+module.fillToPercent.toFixed(2)} %</p>
                </div>
                <div>
                    <div className="input-wrapper regular-border v-spacer-xs">
                        <span className="material-symbols-outlined icon input-icon">percent</span>
                        <input style={{minWidth: "120px"}} placeholder="Fill to [%]" type={"number"} step={"0.1"} min={0} max={100} value={fillTo} onChange={onFillToChange}/>
                    </div>
                    <button className="small-button w-100 solid-button" onClick={onClickFillTo}>Set fill to [%]</button>
                </div>
            </div>
        </div>
    )
}