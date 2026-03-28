// // Frontend examples for sending custom headers to the mock backend
// // Use these in browser code (e.g., console or from components)
//
// export async function echoHeaders() {
//   const res = await fetch('http://localhost:4000/echo-headers', {
//     method: 'GET',
//     headers: {
//       'X-Custom-Header': 'my-custom-value',
//       'Authorization': 'Bearer demo-token-123'
//     }
//   })
//   return res.json()
// }
//
// export async function postEcho(body = { hello: 'world' }) {
//   const res = await fetch('http://localhost:4000/echo', {
//     method: 'POST',
//     headers: {
//       'Content-Type': 'application/json',
//       'X-Custom-Header': 'my-custom-value',
//       'Authorization': 'Bearer demo-token-123'
//     },
//     body: JSON.stringify(body)
//   })
//   return res.json()
// }
