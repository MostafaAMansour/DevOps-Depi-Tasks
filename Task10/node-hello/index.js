const http = require('http');
const fs = require('fs');
const port = process.env.PORT || 3000;

// Create a log file or append to it if it already exists
const logFile = fs.createWriteStream('access.log', { flags: 'a' });

const server = http.createServer((req, res) => {
  res.statusCode = 200;
  const msg = 'Hello Node!\n'
  res.end(msg);

  // Log the access details
  const logEntry = `${new Date().toISOString()} - ${req.method} ${req.url} - ${req.headers['user-agent']}\n`;

  // Write to the log file
  logFile.write(logEntry);

  // Print to the console
  console.log(logEntry);
});

server.listen(port, () => {
  console.log(`Server running on http://localhost:${port}/`);
});
