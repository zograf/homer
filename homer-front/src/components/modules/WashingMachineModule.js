import axios from "axios";
import {API} from "../../environment";
import { useState } from "react";
import {DropDownInput, DropDownSelect} from "../../components/dropdown/DropDownInput";

export default function WashingMachineModule({module, device, token}) {
    const [modes, setModes] = useState([
        { label: "40 degree wash", value: "WASH_40" },
        { label: "90 degree wash", value: "WASH_90" },
        { label: "Rinse", value: "RINSE" },
        { label: "Spin", value: "SPIN" },
    ])

    const onClickMode = () => {
        let payload = new FormData()
        payload.append("deviceId", device.id)
        payload.append("mode", mode)

        axios.post(API + "/device/washingMachine/mode", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
            })
            .catch(e => {
                console.log(e)
            })
    }

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
            return val[3] + ":" + val[4]
        }
    }
    
    const m = (mode) => {
        return modes.filter(item => item.value === mode)[0].label
    }

    return(
        <div className="flex columns space-between">
            <p className="card-title neutral v-spacer-s">Washing machine</p>
            <div className="flex gap-l center">
                <div className="data-grid" style={{ margin: '4px 0' }}>
                    <p className="key-item">Current mode:</p>
                    <p className="value-item">{module.currentMode == null ? "None" : m(module.currentMode)}</p>
                    <p className="key-item">Started:</p>
                    <p className="value-item">{module.currentStart == null ? "None" : d(module.currentStart)}</p>
                    <p className="key-item">Ends:</p>
                    <p className="value-item">{module.currentEnd == null ? "None" : d(module.currentEnd)}</p>
                </div>
                <div>
                    <DropDownSelect
                        placeholder={"Mode"} 
                        options={modes} 
                        callback={modeCallback}/>
                    <button className="small-button w-100 solid-button" onClick={onClickMode}>Change mode</button>
                </div>
            </div>
        </div>
    )
}
