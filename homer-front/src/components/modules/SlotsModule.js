import { useState } from "react";
import axios from "axios";
import { API } from "../../environment";

export default function SlotsModule({module, device, token}) {

    return(
        <div className="flex">
            {module.slots.sort((x, y) => x.id < y.id ? -1 : 1).map((slotInfo, index) => (
                <SlotInfo slotInfo={slotInfo} index={index}/>
            ))}
        </div>
    )
}

function SlotInfo({slotInfo, index}) {
    return (
        <div className="card flex v-spacer-s h-spacer-xs">
            <div className="flex columns space-between gap-s">
                <p className="card-title neutral">Slot {index + 1}</p>
                <div className="flex gap-l">
                    {slotInfo.occupied && <div className="data-grid" style={{marginTop: "0px"}}>
                        <p className="key-item">Capacity:</p>
                        <p className="value-item">{+slotInfo.capacity.toFixed(2)} kWh</p>
                        <p className="key-item">Percent:</p>
                        <p className="value-item">{+slotInfo.percent.toFixed(2)} %</p>
                    </div>}
                    {(!slotInfo.occupied) && <AvailableSlot />}
                </div>
            </div>
        </div>
    )
}

function AvailableSlot() {
    return (
        <div className="flex center justify-center columns dashed-card" style={{minWidth: "100px"}}>
            <p className="tutorial-text neutral">Available</p>
        </div>
    )
}
