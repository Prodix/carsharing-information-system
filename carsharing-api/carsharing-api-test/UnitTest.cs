using System.Runtime.InteropServices;
using System.Security.Cryptography.X509Certificates;
using carsharing_api.Controllers;
using carsharing_api.Entities;
using JWT.Algorithms;
using JWT.Builder;
using Newtonsoft.Json;

namespace carsharing_api_test;

[TestClass]
public class UnitTest
{
    private string _keyBase = "etfpbiaI/tdXSTl36Os6Q3hufDpcSxVwXZYY7lx4Z7g=";
    private string _vectorBase = "autI78dTryrVFHHivDxr5g==";
    
    [TestMethod]
    public void TestCorrectDecryptByNewToken()
    {
        var user = new User();
        user.Email = "test";
        
        var token = JwtBuilder.Create()
            .WithAlgorithm(new RS256Algorithm(
                RuntimeInformation.IsOSPlatform(OSPlatform.Linux) 
                    ? new X509Certificate2("cert.pfx", "123")
                    : AccountController.GetCertificate("CN=CarsharingCert")
            ))
            .AddClaim("user", AccountController.EncryptDataWithAes(JsonConvert.SerializeObject(user), 
                _keyBase, 
                _vectorBase))
            .Encode();

        var decryptedUser = AccountController.DecryptToken(token);
        
        Assert.AreEqual(decryptedUser.Email, user.Email);
    }
    
    [TestMethod]
    public void TestCorrectDecryptByEncryptedToken()
    {
        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoiSzFOUk9WamptdjQ4ZEc5d1FrUFRaMmtrcC9kbFZiOWJaS1RISVlmTDI2UVdYQ25nL3RiXHUwMDJCTlVFMGRpWXRJejRjQmxkeFIvdkc5MlAyYzl1T3U4VGdtL05NZ0ZFZWpKZUdtMXBQS1JcdTAwMkJEallnc2M1cVVcdTAwMkJYZHVBMnlLSEppU044UHMxSzhTd29vUkEzMjR3XHUwMDJCdjBCYVlydFQxM1RKTkZDd0J0MnVnTk5WZ0dGb1NoUTZCM255bERpc01cdTAwMkJxeThubm81M0x0eWtUQVZYV1pyM3FFVWVaRUd5T0VxQ2FBcm9WRTFBUnZGcnN2VmJvbXcyVEZnM2pnZm0vbWovaWtxTm5tSDQifQ.c1CBatRE7RwmMVhqs0438cnTuXaHKiRiPyJVQ7XBVi-ZtcfVp88yPZucvTfv06fQ_f5f8ryK4O8DWjn-ouZwl7Id9uGtIhOxbgWoTc-_zholQib8Ojl3lMiHdRv-B2Gi52zKZcvGk5HYghKHdDKL9m_17x6oC8CFVOwCjvvj6VUUDiNxfz6jpx0lGDvqONPhsW7aL8YmZfcH2YPnMHQYCiIU9YWYPYYzJswnPYjS91z4seAiAVawoeCrt0uHveAVDcvJxfC3_7BdmUroze4-Jsj7uX2JLth9dQ-1eh5nfbwlx-xYai00eEUtgNf1YrZzF3EB5Kye03xokHZVrqzHPA";

        var decryptedUser = AccountController.DecryptToken(token);
        
        Assert.AreEqual(decryptedUser.Email, "test");
    }
    
    [TestMethod]
    public void TestTokenEquality()
    {
        var user = new User();
        user.Email = "test";
        var token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoiSzFOUk9WamptdjQ4ZEc5d1FrUFRaMmtrcC9kbFZiOWJaS1RISVlmTDI2UVdYQ25nL3RiXHUwMDJCTlVFMGRpWXRJejRjQmxkeFIvdkc5MlAyYzl1T3U4VGdtL05NZ0ZFZWpKZUdtMXBQS1JcdTAwMkJEallnc2M1cVVcdTAwMkJYZHVBMnlLSEppU044UHMxSzhTd29vUkEzMjR3XHUwMDJCdjBCYVlydFQxM1RKTkZDd0J0MnVnTk5WZ0dGb1NoUTZCM255bERpc01cdTAwMkJxeThubm81M0x0eWtUQVZYV1pyM3FFVWVaRUd5T0VxQ2FBcm9WRTFBUnZGcnN2VmJvbXcyVEZnM2pnZm0vbWovaWtxTm5tSDQifQ.c1CBatRE7RwmMVhqs0438cnTuXaHKiRiPyJVQ7XBVi-ZtcfVp88yPZucvTfv06fQ_f5f8ryK4O8DWjn-ouZwl7Id9uGtIhOxbgWoTc-_zholQib8Ojl3lMiHdRv-B2Gi52zKZcvGk5HYghKHdDKL9m_17x6oC8CFVOwCjvvj6VUUDiNxfz6jpx0lGDvqONPhsW7aL8YmZfcH2YPnMHQYCiIU9YWYPYYzJswnPYjS91z4seAiAVawoeCrt0uHveAVDcvJxfC3_7BdmUroze4-Jsj7uX2JLth9dQ-1eh5nfbwlx-xYai00eEUtgNf1YrZzF3EB5Kye03xokHZVrqzHPA";
        
        var newToken = JwtBuilder.Create()
            .WithAlgorithm(new RS256Algorithm(
                RuntimeInformation.IsOSPlatform(OSPlatform.Linux) 
                    ? new X509Certificate2("cert.pfx", "123")
                    : AccountController.GetCertificate("CN=CarsharingCert")
            ))
            .AddClaim("user", AccountController.EncryptDataWithAes(JsonConvert.SerializeObject(user), 
                _keyBase, 
                _vectorBase))
            .Encode();

        Assert.AreEqual(newToken, token);
    }
}