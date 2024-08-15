export default function BatteryModule({module, device, token}) {
    return(
        <div className="flex columns space-between">
            <p className="card-title neutral">About</p>
            <div className="data-grid">
                <p className="key-item">Capacity:</p>
                <p className="value-item">{module.capacity} kWh</p>
                <p className="key-item">Value:</p>
                <p className="value-item">{+module.value.toFixed(2)} kWh</p>
                <p className="key-item">Percent:</p>
                <p className="value-item">{+module.percent.toFixed(2)} %</p>
            </div>
        </div>
    )
}