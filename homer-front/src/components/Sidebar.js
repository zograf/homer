import { useEffect, useMemo, useState } from 'react'
import './Sidebar.css'

export function UserSidebar() {
    return(
        <main>
            <div className="sidebar">
                <SidebarTile icon={"logout"} label={"Logout"} path="/login"/>
            </div>
            <div className="sidebar" style={{padding: '18px 0 14px 0'}}>
                <SidebarTile icon={"home_iot_device"} label={"Devices"} path={"/devices"}/>
                <SidebarTile icon={"electric_meter"} label={"View Energy Usage"} path={"/property/consumption"}/>
                <SidebarTile icon={"share"} label={"Shared"} path={"/user/shared"}/>
                <SidebarDevider/>
                <SidebarTile icon={"add_home"} label={"Add Property"} path={"/property/add"}/>
                <SidebarTile icon={"monitor_weight_gain"} label={"Add Device"} path={"/device/add"}/>
            </div>
        </main>
    )
}

export function AdminSidebar() {
    const isSuperAdmin = localStorage.getItem("isSuperAdmin") === "true"

    return(
        <main>
            <div className="sidebar">
                <SidebarTile icon={"logout"} label={"Logout"} path="/login"/>
            </div>
            <div className="sidebar" style={{padding: '18px 0 14px 0'}}>
                { isSuperAdmin && <SidebarTile icon={"person_add"} label={"Register an Admin"} path="/admin/register"/> }
                <SidebarTile icon={"home_work"} label={"Manage Property Requests"} path="/property/requests"/>
                <SidebarTile icon={"electric_meter"} label={"View Energy Usage"} path="/admin/consumption" />
            </div>
        </main>
    )
}

export function SidebarTile({icon, label, path}) {
    const handleClick = () => { window.location.href = path }
    return(
        <div className="sidebar-tile-container" onClick={handleClick}>
            <span className="sidebar-tooltip">{label}</span>
            <span className="material-symbols-outlined icon">{icon}</span>
        </div>
    )
}

export function SidebarDevider() {
    return( <hr style={{width: "calc(100% - 24px)", marginTop: "2px", marginBottom: "2px"}}/> )
}
