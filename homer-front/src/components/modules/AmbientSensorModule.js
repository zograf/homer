import axios from "axios";
import {API} from "../../environment";

export default function AmbientSensorModule({module, device, token}) {
    return(
        <div className="flex columns space-between">
            <p className="card-title neutral">Ambient Sensor</p>
            <div className="data-grid" style={{ margin: '4px 0' }}>
                <p className="key-item">Temperature:</p>
                <p className="value-item">{module.temperatureValue.toFixed(2)} °C</p>
                <p className="key-item">Humidity:</p>
                <p className="value-item">{module.humidityPercent.toFixed(2)} %</p>
            </div>
        </div>
    )
}
