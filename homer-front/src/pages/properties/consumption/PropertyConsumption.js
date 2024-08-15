import {useEffect, useState} from "react";
import axios from "axios";
import {API} from "../../../environment";
import {UserPage} from "../../root/UserPage";
import { AdminPage } from "../../root/AdminPage";
import {DropDownInput, DropDownSelect} from "../../../components/dropdown/DropDownInput";
import ConsumptionGraph from "../components/ConsumptionGraph";
import { CitySelector } from "../add/components/CitySelector";

export function UserConsumptionPage() {

    const [propertyId, setPropertyId] = useState(undefined);
    const token = localStorage.getItem("token")

    const [properties, setProperties] = useState([])

    const getProperties = () => {
        axios.get(API + "/property/accepted", { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => { setProperties(response.data.map((item) => { return { label: item.name, value: item }})) })
            .catch(e => console.log(e))
    }
    useEffect(() => getProperties(), [])
    
    const propertyCallback = (item) => {
        if(item !== undefined){
            setPropertyId(item.id)
        }else{
            setPropertyId(undefined)
        }
    }

    return(
        <UserPage>
            <main className="mh-100">
                <h1 className="page-title">Energy Usage</h1>
                <DropDownSelect placeholder={"Property*"} icon={"home"} options={properties} callback={propertyCallback}/>
                {propertyId && <ConsumptionGraph propertyId={propertyId} cityId={undefined} device={undefined} token={token} />}
            </main>
        </UserPage>
    )
}

export function AdminConsumptionPage() {

    const [propertyId, setPropertyId] = useState(undefined);
    const token = localStorage.getItem("token")

    const [properties, setProperties] = useState([])

    const getProperties = () => {
        axios.get(API + "/property/all", { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => { setProperties(response.data.map((item) => { return { label: item.name, value: item }})) })
            .catch(e => console.log(e))
    }
    useEffect(() => getProperties(), [])
    
    const propertyCallback = (item) => {
        if(item !== undefined){
            setPropertyId(item.id)
        }else{
            setPropertyId(undefined)
        }
    }

    const [cityId, setCityId] = useState(undefined);
    
    const citySelected = (cityLocation) => {
        setCityId(cityLocation.cityId)
    }

    const types = [
        { label: "Property Consumption", value: 0},
        { label: "City Consumption", value: 1},
    ]
    const [type, setType] = useState(undefined)

    const typeCallback = (item) => {
        if(item !== undefined){
            setType(item)
        }else{
            setType(undefined)
        }
    }

    return(
        <AdminPage>
            <main className="mh-100">
                <h1 className="page-title">Energy Usage</h1>
                <DropDownInput placeholder={"Report type"} icon={"report"} options={types} callback={typeCallback}/>
                { type === 0 && <DropDownInput placeholder={"Property"} icon={"home"} options={properties} callback={propertyCallback}/> }
                { type === 1 && <CitySelector token={token} callback={citySelected}/> }
                {(type === 0 && propertyId) && <ConsumptionGraph propertyId={propertyId} cityId={undefined} device={undefined} token={token} />}
                {(type === 1 && cityId) && <ConsumptionGraph propertyId={undefined} cityId={cityId} device={undefined} token={token} />}
            </main>
        </AdminPage>
    )
}