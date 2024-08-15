import { useMemo, useState } from 'react'
import { AdminSidebar, SuperAdminSidebar } from '../../components/Sidebar'
import './AdminPage.css'

export function AdminPage(props) {
    useMemo(() => {
        if (localStorage.getItem("token") == null ||
            localStorage.getItem("token") == '') {

            alert("You must be logged in to continue")
            window.location.href = "/login"
        }
    }, [])

    return(
        <main className="mh-100">
            <div className="sidebar-root">
                <AdminSidebar/>
                <div className="header-root">
                    <div>

                    </div>
                    <div className="page-wrapper">
                        {props.children}
                    </div>
                </div>
            </div>
        </main>
    )
}
