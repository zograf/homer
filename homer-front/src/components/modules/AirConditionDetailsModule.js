import axios from "axios";
import {API} from "../../environment";

export default function AirConditionDetailsModule({module, device, token}) {
    return(
        <div className="flex columns space-between">
            <p className="card-title neutral">Current values</p>
            <div className="data-grid" style={{ margin: '4px 0' }}>
                <p className="key-item">Temperature:</p>
                <p className="value-item">{module.currentTemperature == null ? "None" : module.currentTemperature + " °C"}</p>
                <p className="key-item">Mode:</p>
                <p className="value-item">{module.currentMode == null ? "None" : module.currentMode}</p>
            </div>
        </div>
    )
}
