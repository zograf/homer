import { useMemo } from 'react'
import { UserSidebar } from '../../components/Sidebar'
import './UserPage.css'

export function UserPage(props) {

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
                <UserSidebar/>
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
