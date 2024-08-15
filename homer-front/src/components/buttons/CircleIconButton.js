import "./CircleIconButton.css"
export default function ({icon, onClick}) {
    return(
        <div className="circle-button-tile-container shadow" onClick={onClick}>
            <span className="material-symbols-outlined icon">{icon}</span>
        </div>
    )
}