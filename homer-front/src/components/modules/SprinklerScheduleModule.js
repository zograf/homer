import React, {useEffect, useState} from "react";
import axios from "axios";
import {API} from "../../environment";

export default function SprinklerScheduleModule({deviceId, token, schedule}) {

    const days = [
        {idx: 0, label: "Mon", fullName: "Monday"},
        {idx: 1, label: "Tue", fullName: "Tuesday"},
        {idx: 2, label: "Wed", fullName: "Wednesday"},
        {idx: 3, label: "Thu", fullName: "Thursday"},
        {idx: 4, label: "Fri", fullName: "Friday"},
        {idx: 5, label: "Sat", fullName: "Saturday"},
        {idx: 6, label: "Sun", fullName: "Sunday"}
    ]

    const [module, setModule] = useState(schedule)
    useEffect(() => { update(schedule)}, [schedule])

    const getScheduledTime = (dayIdx) => {
        let time = {
            from : module.monStartTime,
            to: module.monEndTime
        }
        if (dayIdx === 1) time = {
            from : module.tueStartTime,
            to: module.tueEndTime
        }
        else if (dayIdx === 2) time = {
            from : module.wedStartTime,
            to: module.wedEndTime
        }
        else if (dayIdx === 3) time = {
            from : module.thuStartTime,
            to: module.thuEndTime
        }
        else if (dayIdx === 4) time = {
            from : module.friStartTime,
            to: module.friEndTime
        }
        else if (dayIdx === 5) time = {
            from : module.satStartTime,
            to: module.satEndTime
        }
        else if (dayIdx === 6) time = {
            from : module.sunStartTime,
            to: module.sunEndTime
        }

        if(time.from === null || time.to === null || time.from === undefined || time.to === undefined) return time

        return {
            from : timeToStr(time.from),
            to: timeToStr(time.to)
        }
    }

    const timeToStr = (time) => {
        if(Array.isArray(time)) return time[0] + ":" + time[1]
        else return time.split(':')[0] + ":" + time.split(':')[1]
    }

    const update = (m) => {
        setModule(m)
        console.log("Setting Module...")
        console.log(m)
        let newList = []
        for(let i = 0; i < 7; i++) newList.push(m.days[i] === '1')
        console.log(newList)
        console.log(scheduledDays)
        setScheduledDays(newList)
    }

    const [scheduledDays, setScheduledDays] = useState([true, false, false, false, false, false, false])
    const [selectedDays, setSelectedDays] = useState([false, false, false, false, false, false, false])

    const [from, setFrom] = useState("")
    const [to, setTo] = useState("")
    const handleFrom = (e) => setFrom(e.target.value);
    const handleTo = (e) => setTo(e.target.value);
    const isScheduled = (dayIdx) => scheduledDays[dayIdx]
    const isSelected = (dayIdx) => selectedDays[dayIdx]
    const setSelected = (dayIdx) => setSelectedDays((prevState) => {
        let updatedList = []
        updatedList.push(...prevState)
        updatedList[dayIdx] = !updatedList[dayIdx]
        return updatedList
    })
    const addSchedule = () => {
        let selection = ""
        selectedDays.forEach((isSelected) => selection += isSelected ? "1" : "0")

        let payload = new FormData()
        payload.append("deviceId", deviceId)
        payload.append("from", from)
        payload.append("to", to)
        payload.append("days", selection)

        axios.post(API + "/device/sprinklers/schedule/set", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
                update(response.data.modules.filter((module) => module.type === "SPRINKLER_SCHEDULE")[0])
            })
            .catch(e => {
                console.log(e)
            })

        setFrom("")
        setTo("")
        setSelectedDays([false, false, false, false, false, false, false])
    }

    const removeSchedule = (dayIdx) => {
        let payload = new FormData()
        payload.append("deviceId", deviceId)
        payload.append("day", dayIdx)

        axios.post(API + "/device/sprinklers/schedule/remove", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log(response)
                update(response.data.modules.filter((module) => module.type === "SPRINKLER_SCHEDULE")[0])
            })
            .catch(e => {
                console.log(e)
            })
    }

    return(
        <main>
            <div className="flex gap-xs v-spacer-m">
                <div className="card">
                    <p className="card-title">Scheduling</p>
                    <p className="card-label v-spacer-m">Select days and which time those days sprinklers should run</p>

                    <div className="flex gap-xxs v-spacer-m">
                        {days.map(day => <button className={`small-button w-100 ${isScheduled(day.idx) ? 'disabled-outline-button' : isSelected(day.idx) ? 'solid-button' : 'outline-button'}`} onClick={() => setSelected(day.idx)}>{day.label}</button>)}
                    </div>

                    <div style={{display:"grid", gridTemplateColumns:"1fr 0.5fr"}}>
                        <div style={{marginLeft:"4px"}}>
                            <div className="flex gap-s">
                                <p className="card-body" style={{marginTop:"10px"}}>From:</p>
                                <div className="input-wrapper regular-border v-spacer-xs">
                                    <input placeholder="From" value={from} onChange={handleFrom} type="time"/>
                                </div>
                            </div>
                            <div className="flex gap-s">
                                <p className="card-body" style={{marginTop:"10px"}}>&nbsp; &nbsp; &nbsp; To:</p>
                                <div className="input-wrapper regular-border v-spacer-xs">
                                    <input placeholder="To" value={to} min="07:00" onChange={handleTo} type="time"/>
                                </div>
                            </div>
                        </div>
                        <button style={{alignSelf:"flex-end"}} className="solid-accent-button" onClick={addSchedule}>Schedule</button>
                    </div>
                </div>
            </div>

            <div className="flex gap-xs v-spacer-xl">
                {days.map((day) => {
                    if (module.days[day.idx] === '1') return(
                        <div className="card">
                           <div className="flex gap-s">
                               <p className="card-title v-spacer-m">{day.fullName}</p>
                               <p className="circle-button-tile-container material-symbols-outlined icon" style={{width:"32px", height:"32px", fontSize:"1.2rem", paddingRight:"1px"}} onClick={() => removeSchedule(day.idx)}>delete</p>
                           </div>
                           <div className="w-100 flex columns center">
                               <p className="section-title v-spacer-s">{getScheduledTime(day.idx).from}</p>
                               <span className="material-symbols-outlined icon index v-spacer-xs" style={{
                                   width: "30px",
                                   paddingTop: "3px",
                                   marginLeft: "10px"
                               }}>keyboard_arrow_down</span>
                               <p className="section-title" style={{marginBottom: "0px"}}>{getScheduledTime(day.idx).to}</p>
                           </div>
                        </div>
                    )
                })}

            </div>
        </main>
    )
}