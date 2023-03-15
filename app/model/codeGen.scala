package model

object codeGen extends App {
  slick.codegen.SourceCodeGenerator.main(
    Array("slick.jdbc.PostgresProfile",
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost/taskdb?user=postgres&password=admin",
      "/Users/tim/IdeaProjects/playDB/app",
      "model"
    )
  )
}
