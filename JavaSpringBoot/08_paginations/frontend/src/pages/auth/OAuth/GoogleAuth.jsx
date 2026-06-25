import { GoogleLogin } from "@react-oauth/google";
import { useContext, useState } from "react";
import { DataContext } from "../../../context";

export default function Login() {
    const {apiUnAuthPost} = useContext(DataContext);
    const [data, setData] = useState();

  return (
    <GoogleLogin
      onSuccess={async(credentialResponse) => {
         await apiUnAuthPost('/auth/oauth-google',  {
        idToken: credentialResponse.credential
      }, setData);
        console.log(data);
        console.log(credentialResponse);
      }}
      onError={() => {
        console.log("Login Failed");
      }}
    />
  );
}