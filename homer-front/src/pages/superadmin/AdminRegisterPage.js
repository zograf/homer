import {useEffect, useState} from "react";
import { AdminPage } from "../root/AdminPage";
import AdminRegisterComponent from "./components/AdminRegisterComponent";

export default function AdminRegisterPage() {
    return(
        <AdminPage>
            <main className="mh-100">
                <h1 className="page-title">Register an Admin</h1>
                <AdminRegisterComponent />
            </main>
        </AdminPage>
    )
}
