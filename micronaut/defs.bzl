"""Public API for rules_micronaut."""

load("//micronaut/private:common.bzl", _MICRONAUT_JAVACOPTS = "MICRONAUT_JAVACOPTS")
load("//micronaut/private:full_jar_deps.bzl", _micronaut_full_jar_deps = "micronaut_full_jar_deps")
load("//micronaut/private:micronaut_application.bzl", _micronaut_application = "micronaut_application")
load("//micronaut/private:micronaut_library.bzl", _micronaut_library = "micronaut_library")
load("//micronaut/private:micronaut_test.bzl", _micronaut_test = "micronaut_test")

MICRONAUT_JAVACOPTS = _MICRONAUT_JAVACOPTS
micronaut_application = _micronaut_application
micronaut_full_jar_deps = _micronaut_full_jar_deps
micronaut_library = _micronaut_library
micronaut_test = _micronaut_test
