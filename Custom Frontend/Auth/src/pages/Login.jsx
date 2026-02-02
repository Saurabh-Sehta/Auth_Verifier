import React, {use, useContext, useEffect, useState } from 'react'
import { assets } from '../assets/assets'
import { Link, useNavigate } from 'react-router-dom'
import axios from 'axios';
import {AppContext} from '../context/AppContext';
import { toast } from 'react-toastify';

const Login = () => {
  const [isCreateAccount, setIsCreateAccount] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isVerified, setIsVerified] = useState(false);
  const [loading, setLoading] = useState(false);
  const {backendURL, setIsLoggedIn, getUserData} = useContext(AppContext);
  const navigate = useNavigate();
  
  const onSubmitHandler = async (e) => {
    e.preventDefault();
    axios.defaults.withCredentials = true;
    setLoading(true);
    try{
      if (isCreateAccount && isVerified){
        // register APi
        const response = await axios.post(`${backendURL}/register`, {name, email, password});
        if(response.status === 201){
          localStorage.removeItem("name");
          localStorage.removeItem("verifyEmail");
          localStorage.removeItem("emailVerified");
          navigate("/");
          toast.success("Account created successfully.")
        } else{
          toast.error("Email already exits");
        }
      } else {
        //login API
        const response = await axios.post(`${backendURL}/login`, {email, password});
        if(response.status === 200){
          setIsLoggedIn(true);
          getUserData();
          navigate("/");
          toast.success("Logged in successfully.")
        } else {
          toast.error("Email or password is wrong!")
        }
      }
    } catch (err){
        toast.error(err?.response?.data?.message || "Something went wrong");
        console.log(err);
    } finally {
      setLoading(false);
    }
  }

  const handleVerify = async (e) => {
    e.preventDefault();
    if(!email){
      toast.error("Please enter email to verify");
      return;
    }
    try {
      setLoading(true);
      localStorage.setItem("verifyEmail", email);
      localStorage.setItem("name", name);
      localStorage.setItem("emailVerified", "false");
      axios.defaults.withCredentials = false;     
      const response = await axios.post(`${backendURL}/send-otp?email=${email}`);
      if(response.status === 200){
        navigate("/email-verify");
        toast.success("OTP has been sent successfully!")
      } else {
        toast.error("Unable to send OTP!")
      }
    } catch (err) {
      toast.error(err?.response?.data?.message || "Something went wrong!");
      console.log(err);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
  const verified = localStorage.getItem("emailVerified") === "true";

  if (verified) {
    setIsVerified(true);
    setIsCreateAccount(true);
    setName(localStorage.getItem("name") || "");
    setEmail(localStorage.getItem("verifyEmail") || "");
  }
}, []);


  return (
    <>
    <nav className='navbar bg-white px-5 py-4 d-flex justify-content-between align-items-center' >
          <Link to="/" className='d-flex align-items-center gap-2' style={{textDecoration: "none"}}>
            <img src={assets.logo} alt="logo" width={32} height={32}/>
            <span className='fw-bold fs-4 text-dark'>Auth-Verifer</span>
          </Link>
    </nav>

    <div className='position-relative min-vh-100 d-flex justify-content-center align-items-center'
        style={{background:"linear-gradient(90deg, #6a5af6, #8268f9", border: "none"}}
    >
      
      {/* <div style={{position: "absolute", top: "20px", left: "30px", display: "flex", alignItems: "center"}}>

        <Link to="/" style={{
          display: "flex",
          gap: "5",
          alignItems: "center",
          fontWeight: "bold",
          fontSize: "24px",
          textDecoration: "none"
        }}>
          <img src={assets.logo} alt="logo" height={32} width={32}/>
          <span className='fw-bold fs-4 text-light'>Auth-Verifier</span>
        </Link>

      </div> */}

      <div className='card p-4' style={{
        maxWidth: "400px", width: "100%"
      }}>
          <h2 className='text-center mb-4'>
            {isCreateAccount ? "Create Account" : "Login"}
          </h2>
          <form onSubmit={onSubmitHandler}>
            {
              isCreateAccount && (
                <div className='mb-3'>
                  <label htmlFor="fullName" className='for-label'>Full Name</label>
                  <input type="text" id="fullName" className='form-control' placeholder='Enter full name' required onChange={(e) => setName(e.target.value)} value={name}/>
                </div>
              )
            }
            {
              isCreateAccount ?
              (<div className='mb-3'>
                <label htmlFor="email" className='for-label'>Email Id</label>
                <div className='d-flex justify-content-between'>
                    <input type="email" id="email" className='form-control' placeholder='Enter email address' readOnly={isVerified} required onChange={(e) => setEmail(e.target.value)} value={email}/>
                    <button 
                    type='button' 
                    className='btn btn-sm btn-outline-primary m-1'
                    onClick={handleVerify}
                    disabled={loading || isVerified}
                    >{isVerified ? "Verified" : "Verify"}</button>
                </div>                
              </div>) :
              (<div className='mb-3'>
                <label htmlFor="email" className='for-label'>Email Id</label>
                <input type="email" id="email" className='form-control' placeholder='Enter email address' required onChange={(e) => setEmail(e.target.value)} value={email}/>
              </div>)
            }
              
              <div className='mb-3'>
                <label htmlFor="password" className='for-label'>Password</label>
                <input type="password" id="password" className='form-control' placeholder='**********' required onChange={(e) => setPassword(e.target.value)} value={password}/>
              </div>
              {
                !isCreateAccount && (
                    <div className='d-flex justify-content-between mb-3'>
                        <Link to="/reset-password" className='text-decoration-none'>Forgot Password?</Link>
                    </div>
                )
              }
              

              <button type='submit' className='btn btn-primary w-100' disabled={loading || (isCreateAccount && !isVerified)}>
                {isCreateAccount ? "Sign Up" : "Login"}
              </button>
          </form>
          <div className='text-center mt-3'> 
              <p className="mb-0">
                {isCreateAccount ?
                    (<>
                      Already have an Account?{"  "}
                      <span className='text-decoration-underline text-primary' style={{cursor: "pointer"}}
                        onClick={() => setIsCreateAccount(false)}
                      >Login here</span>
                    </>) :
                    (<>
                      Don't have an Account?{"  "}
                      <span className='text-decoration-underline text-primary' style={{cursor: "pointer"}}
                        onClick={() => setIsCreateAccount(true)}
                      >Sign Up</span>
                    </>)
                }
              </p>
          </div>
      </div>
    </div>
    </>
  )
}

export default Login