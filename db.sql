--
-- PostgreSQL database dump
--

-- Dumped from database version 16.2
-- Dumped by pg_dump version 16.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE carsharing_db;
--
-- Name: carsharing_db; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE carsharing_db WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Russian_Russia.1251';


ALTER DATABASE carsharing_db OWNER TO postgres;

\connect carsharing_db

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: car_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.car_type AS ENUM (
    'base',
    'comfort',
    'business'
);


ALTER TYPE public.car_type OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: transport; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transport (
    id integer NOT NULL,
    transport_type public.car_type NOT NULL,
    car_name text NOT NULL,
    car_number character varying(9) NOT NULL,
    car_image_path text NOT NULL,
    is_reserved boolean DEFAULT false NOT NULL,
    gas_level smallint NOT NULL,
    has_insurance boolean NOT NULL,
    is_door_opened boolean NOT NULL,
    longitude real NOT NULL,
    latitude real NOT NULL
);


ALTER TABLE public.transport OWNER TO postgres;

--
-- Name: transport_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.transport_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.transport_id_seq OWNER TO postgres;

--
-- Name: transport_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.transport_id_seq OWNED BY public.transport.id;


--
-- Name: transport id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transport ALTER COLUMN id SET DEFAULT nextval('public.transport_id_seq'::regclass);


--
-- Data for Name: transport; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.transport (id, transport_type, car_name, car_number, car_image_path, is_reserved, gas_level, has_insurance, is_door_opened, longitude, latitude) FROM stdin;
1	base	Daewoo Nexia	К742СМ53	nexia.png	f	100	t	f	58.56117	31.32089
2	base	ИЖ-2126 "Ода"	Е756ЕВ53	izh2126.png	f	100	t	f	58.562798	31.315336
\.


--
-- Name: transport_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.transport_id_seq', 2, true);


--
-- Name: transport transport_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transport
    ADD CONSTRAINT transport_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

