import React, {useEffect, useState} from "react";
import axios from "axios";
import {API} from "../../../../environment";
import {DropDownInput} from "../../../../components/dropdown/DropDownInput";

export function CitySelector({token, callback}) {
    const [countries, setCountries] = useState([])
    const [cities, setCities] = useState([])
    const [selectedCountry, setSelectedCountry] = useState(undefined)
    const [_, setSelectedCity] = useState(undefined)
    const getLocations = () => {
        axios.get(API + "/country", { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => { setCountries(response.data.map((item) => { return { label: item.name, value: item }})) })
            .catch(e => console.log(e))
    }
    useEffect(() => getLocations(), [])

    const countryCallback = (item) => {
        setSelectedCountry(item)
        if(item !== undefined) setCities(item.cities.map((item) => { return { label: item.name, value: item }}))
        else {
            setCities([])
            setSelectedCity(undefined)
        }
        callback({
            countryId: item?.id,
            cityId: undefined
        })
    }
    const cityCallback = (item) => {
        setSelectedCity(item)
        callback({
            countryId: selectedCountry?.id,
            cityId: item?.id
        })

    }
    return(
        <div className="two-input-wrapper">
            <DropDownInput placeholder={"Country*"} icon={"flag"} options={countries} callback={countryCallback}/>
            <DropDownInput placeholder={"City*"} icon={"location_city"} options={cities} callback={cityCallback}/>
        </div>
    )
}