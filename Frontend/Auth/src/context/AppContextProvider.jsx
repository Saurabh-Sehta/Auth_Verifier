import React, { useEffect, useState } from 'react';
import { AppContext } from './AppContext';
import { AppConstants } from '../util/constants';
import axios from 'axios';
import { toast } from 'react-toastify';

const AppContextProvider = ({ children }) => {

    const backendURL = AppConstants.BACKEND_URL;
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userData, setUserData] = useState(null);

    const getUserData = async () => {
        try{
            axios.defaults.withCredentials = true;
            const response = await axios.get(backendURL+"/profile");
            if(response.status === 200){
                setUserData(response.data);
            } else {
                toast.error("Unable to retrive user data ")
            }
        } catch (err){
            toast.error("Something went Wrong!")
        }
    }

    const getAuthState = async () => {
        try {
            axios.defaults.withCredentials = true;
            const response = await axios.get(backendURL+"/isAuthenticated");
            if (response.status === 200 && response.data === true) {
                setIsLoggedIn(true);
                await getUserData();
            } else {
                setIsLoggedIn(false);
            }
        } catch (err) {
            if (err.response) {
                const msg = err.response.data?.message || "Authentication checked failed"; 
                // toast.error(msg);
            } else {
                // toast.error("Something went wrong!");
            }
            setIsLoggedIn(false);
        }
    }

    useEffect(() => {
        getAuthState();
    }, []);

    const contextValue = {
        backendURL,
        isLoggedIn,
        setIsLoggedIn,
        userData,
        setUserData,
        getUserData
    };

    return (
        <AppContext.Provider value={contextValue}>
            {children}
        </AppContext.Provider>
    );
};

export default AppContextProvider;
