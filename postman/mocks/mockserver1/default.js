const http = require("http");
const PORT = process.env.PORT || 4500;
const JSON_HEADER = { "Content-Type": "application/json" };

const readBody = (req) =>
  new Promise((resolve) => {
    let raw = "";
    req.on("data", (c) => (raw += c));
    req.on("end", () => {
      try { resolve(raw ? JSON.parse(raw) : {}); } catch { resolve({}); }
    });
  });

const SEED = [
  { id: 2, title: "Buy groceries", description: null, completed: false, dueDate: null },
  { id: 6, title: "Buy milk", description: "Two bottles", completed: false, dueDate: null },
];

const server = http.createServer(async (req, res) => {
  const { method, url } = req;
  const todos = (await pm.state.get("todos")) || SEED;

  // @endpoint GET /api/todos
  if (method === "GET" && url === "/api/todos") {
    res.writeHead(200, JSON_HEADER);
    return res.end(JSON.stringify(todos));
  }

  // @endpoint POST /api/todos
  if (method === "POST" && url === "/api/todos") {
    const b = await readBody(req);
    const nextId = todos.reduce((m, t) => Math.max(m, t.id), 0) + 1;
    const todo = {
      id: nextId,
      title: b.title || "Untitled",
      description: b.description ?? null,
      completed: b.completed ?? false,
      dueDate: b.dueDate ?? null,
    };
    todos.push(todo);
    await pm.state.set("todos", todos);
    res.writeHead(201, JSON_HEADER);
    return res.end(JSON.stringify(todo));
  }

  res.writeHead(404, JSON_HEADER);
  res.end(JSON.stringify({ error: "Not found" }));
});

server.listen(PORT);