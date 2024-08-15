
export default function SolarPanelSystemModule({module, device, token}) {

    return(
        <div className="flex columns space-between">
            <p className="card-title neutral">About</p>
            <div className="data-grid">
                <p className="key-item">Number of panels:</p>
                <p className="value-item">{module.numPanels}</p>
                <p className="key-item">Area:</p>
                <p className="value-item">{module.area} m2</p>
                <p className="key-item">Efficiency:</p>
                <p className="value-item">{module.efficiency} %</p>
            </div>
        </div>
    )
}