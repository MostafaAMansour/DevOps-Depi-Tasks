package main

import (
    "context"
    "log"
    "net/http"
    "os"

    "github.com/99designs/gqlgen/handler"
    "github.com/shpota/skmz/cors"
    "github.com/shpota/skmz/db"
    "github.com/shpota/skmz/gql"
    "github.com/shpota/skmz/gql/gen"
    "go.mongodb.org/mongo-driver/mongo"
    "go.mongodb.org/mongo-driver/mongo/options"
)

func main() {
    client, err := mongo.Connect(context.TODO(), clientOptions())
    if err != nil {
        log.Fatal(err)
    }
    defer client.Disconnect(context.TODO())

    // Add the logging middleware
    http.Handle("/query", loggingMiddleware(gqlHandler(db.New(client))))
    http.Handle("/playground", loggingMiddleware(
        handler.Playground("GraphQL playground", "/query")),
    )
    http.Handle("/", loggingMiddleware(http.FileServer(http.Dir("/webapp"))))
    
    err = http.ListenAndServe(":8080", nil)
    log.Println(err)
}

func gqlHandler(db db.DB) http.HandlerFunc {
    config := gen.Config{
        Resolvers: &gql.Resolver{DB: db},
    }
    gh := handler.GraphQL(gen.NewExecutableSchema(config))
    if os.Getenv("profile") != "prod" {
        gh = cors.Disable(gh)
    }
    return gh
}

// Middleware function to log each request
func loggingMiddleware(next http.Handler) http.Handler {
    return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        log.Printf("Received request: %s %s from %s\n", r.Method, r.URL.Path, r.RemoteAddr)
        next.ServeHTTP(w, r)
    })
}

func clientOptions() *options.ClientOptions {
    host := "db"
    if os.Getenv("profile") != "prod" {
        host = "localhost"
    }
    return options.Client().ApplyURI(
        "mongodb://" + host + ":27017",
    )
}
